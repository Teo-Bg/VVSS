# Requirements Review Checklist – MyDrinkShop (cu referințe la cerințe)

---

## R01 – Requirements are incomplete. ✅ Yes

| Afirmație | Referință în cerințe |
|---|---|
| Nu se specifică câmpurile unui **produs** (preț, cod, descriere, etc.) | CF1 – „adăugare produs nou" – nu se detaliază ce date are produsul |
| Nu se specifică câmpurile unui **ingredient** (unitate de măsură, etc.) | CF4 – „pentru fiecare ingredient se specifică o cantitate" – nu se spune ce altceva are ingredientul |
| Conceptul de **client** este menționat dar nedefinit | Introducere – „comenzilor plasate de clienți" – clientul nu apare nicăieri altundeva |
| **Bonul de casă** nu are un format definit | CF7 – „se generează bonul de casă care se salvează în format .csv" – fără detalii despre conținut |
| **Raportul zilnic** nu are un format sau conținut definit | CF8 – „se calculează totalul zilei. Datele pot fi exportate în format CSV" – fără detalii |
| Nu se specifică ce se întâmplă când **stocul este insuficient** | CF5 – „actualizarea stocului în urma comenzilor" – nu se descrie cazul de lipsă stoc |

---

## R02 – Requirements are missing. ✅ Yes

| Afirmație | Referință în cerințe |
|---|---|
| **Autentificare / roluri utilizatori** – complet absent | Nicio cerință funcțională sau non-funcțională nu menționează utilizatori/roluri |
| **Validarea datelor** – nicio regulă de validare | CF1–CF8 nu conțin nicio regulă (ex: preț > 0, cantitate > 0, nume unic) |
| **Tratarea erorilor** (fișier corupt, date invalide) – absentă | CNF3 – „persistența în fișiere text" – dar nu se descrie ce se face la erori de citire/scriere |
| **Ștergere în cascadă** – nedefinită | CF1 – „ștergere produs" și CF4 – rețete cu ingrediente: nu se spune ce se întâmplă cu rețetele la ștergerea produsului |
| **Căutare / filtrare produse** – absentă | CF1 – „afișare listă produse" – nu se menționează nicio funcționalitate de căutare sau filtrare |
| **Aprovizionare manuală a stocului** – absentă | CF5 – menționează doar „actualizarea stocului în urma comenzilor", nu și adăugarea de stoc |

---

## R03 – Requirements are incorrect. ✅ Yes

| Afirmație | Referință în cerințe |
|---|---|
| **Contradicție**: CF6 spune că utilizatorul adaugă produse în comandă cu cantitate, dar CF7 spune că aplicația calculează automat conținutul comenzii | CF6 – „fiecare produs are asociată o cantitate" vs. CF7 – „calculeze automat conținutul unei comenzi pe baza rețetelor" |
| **Ambiguitate**: salvarea la „finalul zilei" nu precizează dacă e automată sau manuală | CF8 – „La finalul fiecărei zile se salvează informațiile" – trigger temporal sau acțiune manuală? |
| **„Totalul zilei"** nu este definit clar (sumă prețuri? costuri ingrediente? număr comenzi?) | CF8 – „se calculează totalul zilei" – fără formulă sau definiție |

---

## R04 – Initialization of the system state has not been considered. ✅ Yes

| Afirmație | Referință în cerințe |
|---|---|
| Nu se specifică starea aplicației la **prima rulare** | CNF3 – „persistența în fișiere text" – dar nu se descrie ce se face dacă fișierele nu există |
| Nu există cerință de **date inițiale** (tipuri, categorii, ingrediente predefinite) | CF2 – „fiecare produs trebuie să aparțină unui tip și unei categorii" – dar nu se spune cum sunt create acestea la start |
| Nu se definește **starea inițială a stocurilor** | CF5 – „afișarea cantității disponibile" – dar nu se spune cu ce valoare pornesc stocurile |
| Comportament la **fișiere lipsă sau corupte** nedefinit | CNF3 – „persistența datelor trebuie realizată în fișiere text" |

---

## R05 – The functions have not been defined adequately. ✅ Yes

| Afirmație | Referință în cerințe |
|---|---|
| **CRUD** descris generic, fără comportament exact al fiecărei operații | CF1 – „adăugare, modificare, ștergere, vizualizare" – fără pași, validări sau rezultate așteptate |
| Nu se specifică ce **câmpuri sunt editabile** la modificare | CF1 – „modificare produs existent" – fără detalii |
| **Exportul CSV** nu are format, separator, headere sau locație definite | CF8 – „Datele pot fi exportate în format CSV" – fără nicio specificație tehnică |
| **Generarea bonului** nu are conținut sau structură definită | CF7 – „se generează bonul de casă care se salvează în format .csv" |
| **Anularea unei comenzi** nu este menționată | CF6 – descrie crearea comenzii, dar nicio cerință nu acoperă anularea |

---

## R06 – The user needs are inadequately stated. ✅ Yes

| Afirmație | Referință în cerințe |
|---|---|
| Nu există **fluxuri de utilizare** (use cases, user stories) | Nicio cerință nu descrie un flux complet (ex: cum plasează utilizatorul o comandă pas cu pas) |
| Nu se definesc **tipuri de utilizatori** (casier, manager, admin) | Introducere + toate CF – se vorbește generic despre „utilizator" fără diferențiere de rol |
| Nu există **priorități** ale funcționalităților | CF1–CF8 sunt enumerate fără indicarea importanței relative |
| Interfața este descrisă doar ca **„intuitivă"**, fără specificații | Introducere – „o interfață grafică intuitivă pentru utilizator" – nicio cerință de UI concretă |
| **Ordinea/fluxul** dintre operații nu este specificat | CF6 + CF7: nu se spune în ce ordine se confirmă comanda, se calculează ingredientele, se generează bonul |

---

## R07 – Environment information is inadequate or partially missing. ✅ Yes

| Afirmație | Referință în cerințe |
|---|---|
| Nu se specifică **versiunea Java** | CNF1 – „aplicație desktop Java" – fără versiune |
| Nu se specifică **versiunea JavaFX** | CNF2 – „interfața grafică trebuie realizată folosind JavaFX" – fără versiune |
| Nu se specifică **sistemul de operare** țintă | CNF1 – „aplicație desktop Java" – Windows? Linux? macOS? |
| Nu se specifică **locația fișierelor de date** (director fix, configurabil?) | CNF3 – „persistența în fișiere text" – fără nicio indicație despre locație |
| Nu există cerințe de **performanță** (număr max produse, comenzi, etc.) | Nicio cerință non-funcțională nu acoperă performanța |
| Nu există cerințe de **instalare / deployment** | CNF1 – „aplicație desktop Java" – JAR? installer? fără specificații |
