# Code Review Report — MyDrinkShop
**Data:** 07.03.2026  
**Baza de cod analizată:** `src/main/java/drinkshop/`

---

## Sumar

| Nr. | Checkpoint | Verdict |
|-----|-----------|---------|
| C01 | Decision logic is erroneous or inadequate | **YES** |
| C02 | Branching is erroneous | **YES** |
| C03 | There are undefined loop terminations | N/A |
| C04 | I/O format errors exist | **YES** |
| C05 | Subprogram invocations are violated | **YES** |
| C06 | There are errors in preparing or processing input data | **YES** |
| C07 | Output processing errors exist | **YES** |
| C08 | Error message processing errors exist | **YES** |
| C09 | There is confusion in the use of parameters | **YES** |
| C10 | There are errors in loop counters | N/A |
| C11 | Errors are made in writing out variable names | **YES** |
| C12 | Variable type and dimensions are incorrectly declared | **YES** |

---

## C01 — Decision logic is erroneous or inadequate ✔ YES

### 1. `DrinkShopService.comandaProdus()` — lipsă verificare null pe reteta
```java
Reteta reteta = retetaService.findById(produs.getId());
// Dacă nu există o rețetă, reteta = null
if (!stocService.areSuficient(reteta)) { ... }  // NullPointerException
```
`findById()` returnează `null` dacă produsul nu are o rețetă asociată. Nu există niciun null-check înainte de a folosi rezultatul, ceea ce provoacă `NullPointerException` la runtime.

### 2. `ReceiptGenerator.generate()` și `CsvExporter.exportOrders()` — acces nesigur la primul element
```java
Product p = products.stream()
    .filter((p1) -> i.getProduct().getId() == p1.getId())
    .toList().get(0);  // IndexOutOfBoundsException dacă produsul nu există
```
Dacă un produs a fost șters din repository dar există în comandă, lista filtrată este goală și `.get(0)` aruncă `IndexOutOfBoundsException`. Logica nu tratează cazul de produs lipsă.

### 3. `OrderService.computeTotal()` — dereferențiere null potențială
```java
.mapToDouble(i -> productRepo.findOne(i.getProduct().getId()).getPret() * i.getQuantity())
```
`productRepo.findOne()` poate returna `null` (produsul a fost șters). Apelul `.getPret()` pe `null` provoacă `NullPointerException`.

---

## C02 — Branching is erroneous ✔ YES

### 1. `RetetaValidator.validate()` — execuție continuă după detecția null
```java
if (ingrediente == null || ingrediente.isEmpty())
    errors.accumulateAndGet("Ingrediente empty!\n", String::concat);

// EROARE: dacă ingrediente == null, linia de mai jos provoacă NullPointerException
ingrediente.stream()
    .filter(entry -> entry.getCantitate() <= 0)
    .forEach(entry -> { ... });
```
Ramura `if (ingrediente == null ...)` adaugă un mesaj de eroare dar **nu oprește execuția**. Linia imediat următoare apelează `ingrediente.stream()` care va arunca `NullPointerException` dacă `ingrediente` este `null`.

### 2. `DrinkShopController.onAddProduct()` — logică redundantă `else if` după `return`
```java
if (r == null) {
    // ...
    return;
} else          // <-- `else` este redundant, `return` deja a ieșit din metodă
if (service.getAllProducts().stream()...) { ... }
```
Structura `else-if` este incorect formatată și redundantă: ramura `else` nu este necesară deoarece prima ramură termină cu `return`. Deși funcționează corect, stilul creează confuzie la citire.

---

## C03 — There are undefined loop terminations ✔ N/A

Toate buclele din cod au condiții clare de terminare:
- `while (index < elems.length)` în `FileRetetaRepository.extractEntity()` — mărginit de lungimea array-ului.
- Bucle `for-each` standard în `StocService`, `FileAbstractRepository`, `OrderValidator`.
- `for (Stoc s : ingredienteStoc)` cu `if (ramas <= 0) break;` — condiție de ieșire explicită.

Nu s-au identificat probleme la terminarea buclelor.

---

## C04 — I/O format errors exist ✔ YES

### 1. `FileStocRepository` — delimiter inconsistent față de restul repository-urilor
```java
// FileStocRepository — delimiter ";"
String[] elems = line.split(";");

// FileProductRepository, FileOrderRepository, FileRetetaRepository — delimiter ","
String[] elems = line.split(",");
```
Inconsistența delimitatorilor între repository-uri face formatele de fișiere incompatibile și îngreunează procesarea externă.

### 2. `FileStocRepository` — roundtrip I/O defect (bug critic)
```java
// Scriere: getCantitate() returnează double → scrie "5.0" în fișier
return entity.getId() + ";" + entity.getIngredient() + ";" +
       entity.getCantitate() + ";" + ...;   // → "1;zahar;5.0;2.0"

// Citire: Integer.parseInt("5.0") → NumberFormatException!
int cantitate = Integer.parseInt(elems[2]);
int stocMinim = Integer.parseInt(elems[3]);
```
`getCantitate()` returnează `double`, deci valoarea scrisă are forma `"5.0"`. La recitire, `Integer.parseInt("5.0")` aruncă `NumberFormatException`, crash-ând aplicația la pornire.

### 3. `CsvExporter` — import neutilizat și format CSV invalid
```java
import java.util.Date;   // neutilizat — LocalDate este folosit în schimb
```
Fișierul CSV exportat amestecă rânduri de date tabulare cu rânduri de sumar (ex. `"total order: 35.0 RON"`, `"-----"`), rezultând un fișier care **nu este un CSV valid** și nu poate fi importat în aplicații externe.

---

## C05 — Subprogram invocations are violated ✔ YES

### 1. Validatorii definiți nu sunt niciodată apelați (cod mort)
Clasele `OrderValidator`, `ProductValidator`, `RetetaValidator`, `StocValidator`, `OrderItemValidator` sunt implementate corect, dar **nicio clasă de serviciu nu le apelează**. Serviciile `OrderService`, `ProductService`, `RetetaService`, `StocService` nu conțin nicio apelare la `validate()`.

```java
// OrderService.addOrder() — validare lipsă
public void addOrder(Order o) {
    orderRepo.save(o);  // nicio validare aplicată
}

// ProductService.addProduct() — validare lipsă
public void addProduct(Product p) {
    productRepo.save(p);  // nicio validare aplicată
}
```

### 2. `DrinkShopService.comandaProdus()` — potențial apel pe null
```java
Reteta reteta = retetaService.findById(produs.getId());
// Nu există verificare: dacă reteta == null, apelul următor este invalid
stocService.areSuficient(reteta);
```

---

## C06 — There are errors in preparing or processing input data ✔ YES

### 1. `DrinkShopController` — parsare numerică fără gestionarea erorilor
```java
// onAddProduct(), onUpdateProduct()
Double.parseDouble(txtProdPrice.getText());   // NumberFormatException dacă input ≠ număr

// onAddNewIngred()
Double.parseDouble(txtNewIngredCant.getText()); // idem
```
Niciun `try-catch` sau validare prealabilă. Dacă utilizatorul introduce text non-numeric, aplicația aruncă `NumberFormatException` necapturat, fără feedback vizual.

### 2. `DrinkShopController.onAddNewReteta()` — generare ID nesigură
```java
Reteta r = new Reteta(service.getAllRetete().size() + 1, new ArrayList<>(newRetetaList));
```
Folosind `size + 1` ca ID: dacă există 3 rețete și rețeta cu ID=2 a fost ștearsă, `size()` returnează 2, deci noul ID va fi 3, **conflictând cu ID-ul existent**.

### 3. `DrinkShopController` — ID comandă hardcodat
```java
private Order currentOrder = new Order(1);  // ID fix = 1
```
La pornirea aplicației, dacă există deja o comandă cu ID=1 în fișierul `orders.txt`, aceasta va fi **suprascrisă** când utilizatorul finalizează prima comandă.

---

## C07 — Output processing errors exist ✔ YES

### 1. `Order` — metode duplicate cu aceeași funcționalitate
```java
public double getTotalPrice() { return totalPrice; }
public double getTotal()      { return totalPrice; }  // duplicat redundant
```
Același câmp este expus prin două metode diferite, creând ambiguitate. `DailyReportService` folosește `Order::getTotal` iar `ReceiptGenerator` folosește `o.getTotalPrice()`.

### 2. `CsvExporter.exportOrders()` — output CSV neconform
```
OrderId,Product,Quantity,Price      ← header CSV valid
1,Espresso,2,7.0                    ← rând date valid
total order: 7.0 RON                ← rând non-CSV, invalid!
-------------------------------     ← rând non-CSV, invalid!
```
Un fișier CSV corect ar trebui să conțină exclusiv rânduri tabelare. Rândurile de sumar strica structura fișierului.

### 3. `ReceiptGenerator.generate()` — concatenare string redundantă în StringBuilder
```java
sb.append(p.getNume() + ": ");   // + concatenare în interiorul append()
```
Minor: ar trebui `sb.append(p.getNume()).append(": ")` pentru consistență cu restul codului.

---

## C08 — Error message processing errors exist ✔ YES

### 1. `DrinkShopController.onAddProduct()` — tip alertă greșit
```java
Alert alert = new Alert(Alert.AlertType.INFORMATION);
alert.setTitle("Error");   // Tip INFORMATION pentru o condiție de eroare!
```
O condiție de eroare (rețetă neselectată) folosește `INFORMATION` în loc de `ERROR` sau `WARNING`, transmițând un semnal vizual incorect utilizatorului.

### 2. `FileAbstractRepository` — excepții I/O înghițite silențios
```java
} catch (IOException e) {
    e.printStackTrace();   // Eroarea este logată în consolă, dar execuția continuă
}
```
Metoda `loadFromFile()` poate eșua (fișier lipsă, permisiuni) fără a anunța caller-ul. Repository-ul va rămâne gol fără nicio indicație că datele nu au fost încărcate.

### 3. `DrinkShopService.comandaProdus()` — excepție necaptată în controller
```java
// DrinkShopService
throw new IllegalStateException("Stoc insuficient pentru produsul: " + produs.getNume());

// DrinkShopController — onFinalizeOrder() nu capturează această excepție
service.addOrder(currentOrder);  // dacă stocul e insuficient → crash silențios
```
Excepția propagată nu este prinsă în interfața grafică, ceea ce poate rezulta în crash neașteptat al event handler-ului JavaFX.

### 4. `RetetaValidator` — mesaj de eroare fără separator
```java
errors.accumulateAndGet("[" + entry.getDenumire() + "]" + "cantitate negativa sau zero", ...);
// Rezultat: "[zahar]cantitate negativa sau zero" — lipsă spațiu între ] și text
```

---

## C09 — There is confusion in the use of parameters ✔ YES

### 1. `DrinkShopService` — parametru de constructor cu nume înșelător
```java
public DrinkShopService(
    Repository<Integer, Product> productRepo,
    Repository<Integer, Order>   orderRepo,
    Repository<Integer, Reteta>  retetaRepo,
    Repository<Integer, Stoc>    stocService   // ← ar trebui „stocRepo"
) {
    this.stocService = new StocService(stocService);  // confuzie: stocService ≠ StocService
}
```
Parametrul se numește `stocService` (convenție de serviciu) deși este un `Repository`. Toate celelalte urmează convenția `...Repo`. Linia de atribuire arată ca o autoatribuire.

### 2. `RetetaValidator` — folosire nejustificată a `AtomicReference` în context single-thread
```java
AtomicReference<String> errors = new AtomicReference<>("");
errors.accumulateAndGet("...", String::concat);
```
`OrderValidator` folosește corect un simplu `String errors = ""` cu concatenare. `RetetaValidator` folosește `AtomicReference` fără nicio justificare concurentă, complicând inutil codul.

---

## C10 — There are errors in loop counters ✔ N/A

Contoarele de buclă sunt gestionate corect:
- `int index = 1` în `FileRetetaRepository.extractEntity()` pornește de la 1 (corect, sare ID-ul), se incrementează cu `index++` la fiecare iterație, mărginit de `elems.length`.
- `double ramas` în `StocService.consuma()` este decrementat corect cu `ramas -= deScazut`.

Nu s-au identificat erori la contoarele de buclă.

---

## C11 — Errors are made in writing out variable names ✔ YES

### 1. Parametru de constructor inconsistent cu convenția din clasă
```java
// DrinkShopService.java
Repository<Integer, Stoc> stocService  // ← ar trebui stocRepo (ca productRepo, orderRepo, retetaRepo)
```

### 2. Parametri lambda inconsistenți pentru același tip
```java
// În DrinkShopController.initialize():
productList.stream().filter(pr -> pr.getId() == prodId)   // "pr" pentru Product

// În onAddProduct():
service.getAllProducts().stream().filter(p -> p.getId() == r.getId()) // "p"

// În ReceiptGenerator și CsvExporter:
products.stream().filter((p1) -> ...)   // "p1"
```
Același tip `Product` este referit în lambda-uri ca `pr`, `p` și `p1` în locuri diferite — lipsă de consistență în numirea variabilelor.

### 3. Variabila `e` — refolosire confuză în contexte diferite
```java
// StocService.consuma() — e = IngredientReteta
for (IngredientReteta e : reteta.getIngrediente()) { ... }

// OrderValidator.validate() — e = ValidationException
} catch (ValidationException e) { ... }
```
Același nume `e` este utilizat pentru tipuri complet diferite în aceeași bază de cod, creând confuzie la citire.

---

## C12 — Variable type and dimensions are incorrectly declared ✔ YES

### 1. `Stoc` — inconsistență tipuri câmpuri vs. parametri constructor/setter (bug critic)
```java
// Câmpuri declarate ca double:
private double cantitate;
private double stocMinim;

// Constructor primește int:
public Stoc(int id, String ingredient, int cantitate, int stocMinim) { ... }

// Setter primește int pentru câmp double:
public void setStocMinim(int stocMinim) { this.stocMinim = stocMinim; }
```
Câmpurile sunt `double` dar constructorul și setterul acceptă `int`. Aceasta creează o inconsistență semantică: dacă `cantitate` trebuie să suporte valori fracționare (ex. 2.5L), constructorul nu permite asta direct.

### 2. `FileStocRepository` — deserializare cu tip greșit (bug critic, corelat cu C04)
```java
// extractEntity(): citit ca int
int cantitate = Integer.parseInt(elems[2]);   // "5.0" → NumberFormatException

// createEntityAsString(): scris ca double
entity.getCantitate()   // → "5.0" (double)
```
Roundtrip-ul fișier → obiect → fișier este rupt: `getCantitate()` scrie `double` (`5.0`), dar `Integer.parseInt` nu poate citi `"5.0"`, cauzând `NumberFormatException` la fiecare pornire a aplicației dacă stocurile au fost modificate.

### 3. `StocService.consuma()` — trunchiere implicită double → int
```java
s.setCantitate((int)(s.getCantitate() - deScazut));
```
Câmpul `cantitate` este `double`, dar valoarea calculată este trunchiată explicit la `int` înainte de a fi salvată. Precizia fracționară se pierde inutil (ex. 2.7 → 2).

---

## Concluzie

**9 din 12 criterii** au defecte identificate. Cele mai critice probleme sunt:

1. **C04 / C12** — Roundtrip I/O defect în `FileStocRepository`: `double` scris, `Integer.parseInt` la citire → crash la startup.
2. **C01 / C05** — Null pointer în `DrinkShopService.comandaProdus()` când rețeta lipsește; validatorii nu sunt niciodată apelați.
3. **C06** — Parsarea numerică din UI fără try-catch cauzează crash necontrolat la input greșit.
4. **C08** — Excepțiile I/O sunt înghițite silențios în `FileAbstractRepository`; `IllegalStateException` din service nu e capturată în controller.
