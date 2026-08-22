# ClanSystem

Plugin Bukkit/Paper per la gestione di clan con sistema di territori e integrazione con PlaceholderAPI.

Il sistema di territori è custom. Implementarlo da zero permette di gestire i chunk claimati con un indice in memoria, senza dipendenze esterne e senza il costo di usare l'API di un altro plugin a ogni evento.

---



## Comandi

Alias del comando principale: `/cl`

| Comando | Ruolo minimo | Descrizione                                                  |
|---|---|--------------------------------------------------------------|
| `/clan create <nome> <tag>` | — | Crea un clan, il fondatore diventa Leader                    |
| `/clan info [clan]` | — | Informazioni sul proprio clan o su un altro                  |
| `/clan join <clan>` | — | Accetta un invito ricevuto                                   |
| `/clan invite <player>` | Officer | Invita un giocatore online                                   |
| `/clan kick <player>` | rango superiore | Espelle un membro di rango inferiore                         |
| `/clan promote <player>` | Leader | Promuove il player                                           |
| `/clan demote <player>` | Leader | Degrada il player                                            |
| `/clan transfer <player>` | Leader | Trasferisce la leadership (doppia conferma)                  |
| `/clan leave` | — | Esce dal clan (non disponibile al Leader)                    |
| `/clan disband` | Leader | Scioglie il clan (doppia conferma)                           |
| `/clan claim` | Officer | Reclama il chunk corrente                                    |
| `/clan unclaim` | Officer | Libera il chunk corrente                                     |
| `/clan chunks` | Leader | Elenca i territori del clan                                  |
| `/clan sethome` | Leader | Imposta la home (solo dentro un proprio territorio)          |
| `/clan home` | — | Teletrasporto alla home, con countdown                       |
| `/clan chat [messaggio]` | — | Invia un messaggio, o attiva/disattiva la modalità chat clan |

`/clanchat` (alias `/cc`) è una scorciatoia equivalente a `/clan chat`.

Tutti i sottocomandi hanno tab completion.

### Ruoli

Tre ruoli con gerarchia a peso: **Membro** (0) → **Officer** (1) → **Leader** (2).

Il confronto avviene in due modi diversi a seconda del caso d'uso:

- `isAtLeast(ruolo)` per comandi limitati a ranghi bassi
- confronto sui pesi per le azioni fra membri 

Le azioni distruttive (`disband`, `transfer`) richiedono di ripetere il comando entro un timeout configurabile.

---

## Territori

I claim funzionano a livello di **chunk**. Ogni chunk può appartenere a un solo clan.

Le protezioni si attivano quando un giocatore agisce dentro un territorio di cui non fa parte, o di cui fa parte ma con un ruolo insufficiente:

| Protezione | Evento coperto |
|---|---|
| `build` | Rottura e piazzamento blocchi |
| `interact` | Click destro su blocchi (porte, bauli, leve) |
| `pvp` | Danno fra giocatori, **inclusi i proiettili** |
| `mob-spawning` | Spawn naturale dei mob |
| `explosions` | TNT, creeper, letti nel Nether |
| `buckets` | Secchi e propagazione di acqua/lava oltre il confine |
| `entities` | Quadri, item frame, armor stand |
| `fire-spread` | Propagazione del fuoco e combustione blocchi |
| `trampling` | Calpestio delle coltivazioni |


**Le esplosioni non vengono annullate, vengono "filtrate".** Una TNT può coinvolgere blocchi di più chunk contemporaneamente. Annullare l'evento bloccherebbe l'esplosione anche fuori dai territori; invece i blocchi protetti vengono rimossi dalla lista di quelli distrutti. L'esplosione avviene normalmente e si ferma al confine del claim.

**I fluidi vengono fermati al confine, non alla sorgente.** Controllare solo dove viene svuotato il secchio è aggirabile: basta versare fuori e lasciar scorrere dentro.

---

## Placeholders

Richiedono PlaceholderAPI. Se il plugin non è presente, ClanSystem parte comunque e si limita a non registrare l'espansione.

| Placeholder | Output |
|---|---|
| `%clans_player_clan%` | Nome del clan, o `Nessuno` |
| `%clans_player_tag%` | Tag del clan |
| `%clans_player_role%` | Ruolo (Membro / Officer / Leader) |
| `%clans_clan_members_online%` | Numero di membri online |

---

## Database

### Perché tre tabelle
I dati sono divisi in 3 tabelle invece di stare in
un'unica tabella. Con una tabella sola il nome del clan, il tag e la home sarebbero
ripetuti su ogni riga: rinominare un clan vorrebbe dire aggiornare venti righe
diverse.
Ci sarebbe anche un problema pratico: un clan ha molti membri e molti territori, ma
in una casella di una tabella ci sta un valore solo. Servirebbe infilare una lista
dentro un singolo campo, cosa che MariaDB non permette di fare in modo utile.
Con tre tabelle il nome del clan è scritto in un posto solo, e le altre due si
limitano a fare riferimento a quel clan.

### Come sono collegate le tabelle
Ogni clan ha un `id` auto-increment come primary key. Membri e territori lo
referenziano tramite `clan_id`, dichiarata come foreign key.
Nella tabella dei membri la primary key non è un id generato ma l'UUID del giocatore:
così è lo schema stesso a impedire che un giocatore appartenga a due clan, senza
doverlo controllare nel codice.

### Vincoli
`UNIQUE (world, chunk_x, chunk_z)` su `clan_claims`: due clan non possono reclamare
lo stesso chunk nemmeno se un bug nel plugin lo permettesse.
`ON DELETE CASCADE` su entrambe le foreign key: sciogliere un clan è un solo
`DELETE FROM clans`, membri e territori li rimuove il database.

---

## Come funziona

### 1. Connessione al database
La prima cosa che il plugin fa all'avvio è connettersi al database. Se la connessione non riesce, il plugin si spegne da solo invece di partire lo stesso. Senza database i clan esistenti non verrebbero caricati e i giocatori risulterebbero senza clan. Se il plugin continuasse a funzionare invece di venir disabilitato, i giocatori potrebbero creare altri clan e fare azioni che però non verrebbero salvati nel database e quindi non esisterebbero al riavvio del server.

### 2. Caricamento in memoria
Subito dopo la connessione, il plugin carica dal database tutti i dati dei clan e li mette nelle mappe dei manager. Da quel momento le letture avvengono
sempre in memoria: per esempio quando un giocatore rompe un blocco, per sapere di chi è quel
chunk il plugin guarda la mappa, non fa una query.
Il motivo è che le protezioni girano dentro gli eventi di Bukkit, sul main thread, lo stesso che fa andare avanti il gioco. Una query è lenta
rispetto a un accesso in memoria, e farne una a ogni blocco rotto significherebbe
rallentare il server per tutti.
Il database viene toccato solo in scrittura. Quando un giocatore compie un'azione, il dato entra subito nella mappa e la scrittura sul database
parte in parallelo su un altro thread: il giocatore riceve
la risposta all'istante e il server non aspetta il database.

### 3. I manager
Il plugin è "diviso" in cinque manager, ognuno responsabile di una cosa sola:
i clan e i membri, i territori, gli inviti, la chat, i teletrasporti in corso. Sono
dati diversi quindi tenerli in classi separate evita di avere un'unica classe enorme che fa tutto, se c'è un problema è anche più facile intervenire.
`ClanManager` tiene due mappe con gli stessi clan dentro, una per nome e una per
giocatore. La seconda serve a rispondere subito alla domanda "in che clan è questo
giocatore?", che è la prima cosa che fa quasi ogni comando: con la sola mappa per
nome bisognerebbe scorrere tutti i clan e per ognuno tutti i membri.

### 4. I comandi
Bukkit conosce un solo comando, `/clan`. Quello che viene dopo, `create`, `kick`,
`claim`, per Bukkit sono solo parte degli args. `ClanCommand` le riceve, guarda la prima e
passa le altre alla classe che gestisce quel sottocomando; ogni sottocomando è una
classe a sé che implementa l'interfaccia `SubCommand`.
L'alternativa era un unico metodo con dentro una catena di if, uno per comando: con
sedici comandi sarebbe diventato un file enorme da modificare ogni volta. Così invece per
aggiungere un comando basta scrivere una classe nuova e registrarla con una riga
in `ClanSystem`, senza toccare niente di quello che già funziona.

### 5. Le protezioni
Registrati i listener, il plugin reagisce agli
eventi e basta: Bukkit lo avvisa quando avviene un evento.
Quando arriva un evento dentro un territorio, `ClaimProtectionListener` chiede a
`ClaimManager` di chi è quel chunk. Se non è di nessuno lascia passare. Se è claimato controlla se il giocatore è un suo membro e se il suo ruolo è
sufficiente per quell'azione.
`ClaimManager` risponde subito perché ha una mappa che va dal chunk al clan che
lo possiede: senza, per ogni blocco rotto bisognerebbe scorrere tutti i clan e per
ognuno tutti i loro territori.

### 6. Placeholder e spegnimento

Per ultimo il plugin registra i placeholder, ma solo se PlaceholderAPI è presente sul
server: essendo una dipendenza opzionale, se manca il plugin parte
lo stesso e si limita a non registrare l'espansione.
Allo spegnimento chiude il pool di connessioni al database. I dati non vanno salvati
in quel momento perché sono già stati scritti man mano che le azioni avvenivano.