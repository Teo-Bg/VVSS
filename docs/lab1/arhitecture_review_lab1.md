# Architecture Review Report – MyDrinkShop
**Date:** 07.03.2026  
**Project:** MyDrinkShop (Java/JavaFX, Maven)

---

## A01 – Is the overall organization of the program clear, including good architectural overview?
### ✅ YES

The project follows a **clear layered architecture**:

| Layer | Package | Classes |
|---|---|---|
| Domain (Model) | `drinkshop.domain` | `Product`, `Order`, `OrderItem`, `Reteta`, `Stoc`, `IngredientReteta`, `CategorieBautura`, `TipBautura` |
| Repository (Data Access) | `drinkshop.repository`, `drinkshop.repository.file` | `Repository<ID,E>`, `AbstractRepository`, `FileAbstractRepository`, `FileProductRepository`, `FileOrderRepository`, `FileRetetaRepository`, `FileStocRepository` |
| Service (Business Logic) | `drinkshop.service` | `DrinkShopService`, `ProductService`, `OrderService`, `RetetaService`, `StocService`, `DailyReportService` |
| UI (Presentation) | `drinkshop.ui` | `DrinkShopApp`, `DrinkShopController` |
| Utilities | `drinkshop.export`, `drinkshop.receipt`, `drinkshop.reports` | `CsvExporter`, `ReceiptGenerator`, `DailyReportService` |
| Validation | `drinkshop.service.validator` | `Validator<T>`, `ProductValidator`, `OrderValidator`, `StocValidator`, `RetetaValidator`, `OrderItemValidator`, `ValidationException` |

**Minor issue:** `DailyReportService` is placed in a separate `reports` package but is also instantiated inside `DrinkShopService`, creating an unclear ownership. It could be considered part of the service layer.

---

## A02 – Is the subsystem and package partitioning and layering logically consistent?
### ✅ YES (with minor issues)

The layering is mostly consistent:
- **Domain** → no dependencies on other layers ✅
- **Repository** → depends only on Domain ✅
- **Service** → depends on Repository and Domain ✅
- **UI** → depends only on Service ✅

**Issues found:**

| Issue | Location |
|---|---|
| `CsvExporter` and `ReceiptGenerator` are **utility/static classes** but are called directly from `DrinkShopService`, bypassing service abstraction | `DrinkShopService.java` lines 70–76 |
| `DrinkShopService` is a **facade service** that directly instantiates sub-services internally (`new ProductService(...)`, `new OrderService(...)`) instead of receiving them via constructor injection – tight coupling | `DrinkShopService.java` lines 24–29 |
| `FileAbstractRepository` calls `loadFromFile()` in constructor in subclasses but the comment `//loadFromFile();` is left in parent – inconsistent initialization pattern | `FileAbstractRepository.java` line 14 |
| `FileStocRepository` uses `;` as separator while `FileProductRepository` and `FileRetetaRepository` use `,` – inconsistent file format across repositories | `FileStocRepository.java` vs `FileProductRepository.java` |

---

## A03 – Does the architecture account for all of the requirements?
### ✅ YES (partially – some requirements are not covered)

| Requirement | Covered? | Notes |
|---|---|---|
| CF1 – CRUD Products | ✅ Yes | `ProductService` + `FileProductRepository` |
| CF2 – Product has type and category | ✅ Yes | `Product` has `TipBautura` and `CategorieBautura` |
| CF3 – CRUD types and categories | ❌ No | `CategorieBautura` and `TipBautura` are **enums** – they cannot be added/modified/deleted at runtime. No service or repository exists for them. |
| CF4 – Recipe with ingredients | ✅ Yes | `Reteta`, `IngredientReteta`, `RetetaService`, `FileRetetaRepository` |
| CF5 – Stock management | ✅ Yes | `Stoc`, `StocService`, `FileStocRepository` |
| CF6 – Order creation | ✅ Yes | `Order`, `OrderItem`, `OrderService` |
| CF7 – Auto-calculate order from recipes + receipt in CSV | ⚠️ Partial | Recipe-based stock check exists (`DrinkShopService.comandaProdus`), but **receipt is only generated as a String** and **never saved to a .csv file** |
| CF8 – Daily report in CSV | ✅ Yes | `CsvExporter.exportOrders()` + `DailyReportService` |
| CNF1 – Desktop Java app | ✅ Yes | JavaFX application |
| CNF2 – JavaFX | ✅ Yes | `DrinkShopApp extends Application` |
| CNF3 – File persistence | ✅ Yes | All `File*Repository` classes |
| CNF4 – Repository Pattern | ✅ Yes | `Repository<ID,E>` interface + implementations |
| CNF5 – Service layer separate from UI | ✅ Yes | `DrinkShopService` is injected into controller |
| CNF6 – OOP principles | ⚠️ Partial | See A08 |

---

## A04 – Are the classes in a subsystem supporting the services identified for the subsystem?
### ✅ YES (with one gap)

| Subsystem | Expected Services | Classes Present | Gap |
|---|---|---|---|
| Product | Add, Update, Delete, List, Filter | `ProductService`, `FileProductRepository`, `ProductValidator` | None |
| Recipe | Add, Update, Delete, List, FindById | `RetetaService`, `FileRetetaRepository`, `RetetaValidator` | None |
| Stock | Add, Update, Delete, List, CheckSufficiency, Consume | `StocService`, `FileStocRepository`, `StocValidator` | No manual stock replenishment exposed in UI |
| Order | Add, Update, Delete, List, ComputeTotal, AddItem, RemoveItem | `OrderService`, `FileOrderRepository`, `OrderValidator` | `deleteOrder` exists in service but **not exposed in UI** |
| Receipt | Generate | `ReceiptGenerator` | Receipt **not saved to file** |
| Daily Report | TotalRevenue, TotalOrders, Export CSV | `DailyReportService`, `CsvExporter` | None |

---

## A05 – Is there a coherent error handling strategy provided?
### ⚠️ NO (partially)

**What exists:**
- `Validator<T>` interface + `ValidationException` class defined in `drinkshop.service.validator`
- Validators defined for: `Product`, `Order`, `OrderItem`, `Stoc`, `Reteta`
- Stock check throws `IllegalStateException` in `DrinkShopService.comandaProdus()` and `StocService.consuma()`
- File I/O errors are caught in `FileAbstractRepository` via `try-catch(IOException)` which calls `e.printStackTrace()`

**Critical issues:**

| Issue | Location |
|---|---|
| **Validators are defined but NEVER called** – `ProductValidator`, `OrderValidator`, etc. are created but none of the service methods (`addProduct`, `addOrder`, etc.) invoke `.validate()` | `ProductService.addProduct()`, `OrderService.addOrder()` |
| `FileAbstractRepository` swallows IO exceptions silently with `e.printStackTrace()` – no propagation, no user feedback | `FileAbstractRepository.java` lines 25, 37 |
| `FileAbstractRepository` wraps write errors in `RuntimeException` for write but uses `e.printStackTrace()` for read – **inconsistent** | lines 25 vs 37 |
| UI controller catches exceptions only for missing product/quantity selection; all other exceptions (parse errors, null references) are **uncaught** | `DrinkShopController.java` |
| `Double.parseDouble()` in controller fields has **no try-catch** – a non-numeric input will crash the app | `DrinkShopController.java` lines `onAddProduct()`, `onAddNewIngred()` |

---

## A06 – Have classic design patterns been considered where they might be incorporated into the architecture?
### ✅ YES (partially)

| Pattern | Used? | Where |
|---|---|---|
| **Repository Pattern** | ✅ Yes | `Repository<ID,E>` interface + `AbstractRepository` + `File*Repository` implementations |
| **Template Method Pattern** | ✅ Yes | `FileAbstractRepository` defines `loadFromFile()`/`writeToFile()` with abstract `extractEntity()` / `createEntityAsString()` for subclasses |
| **Facade Pattern** | ✅ Yes | `DrinkShopService` acts as a facade over `ProductService`, `OrderService`, `RetetaService`, `StocService` |
| **MVC Pattern** | ✅ Yes | `DrinkShopController` (Controller) + FXML (View) + Service (Model) |
| **Strategy / Validator Pattern** | ⚠️ Partial | `Validator<T>` interface defined but validators are **not integrated** into service methods |
| **Factory Pattern** | ❌ No | Repository instances are manually created in `DrinkShopApp.start()` – a Factory/Builder could be used |
| **Observer Pattern** | ❌ No | UI refreshes are done manually via `initData()` calls instead of using JavaFX bindings or observable properties |
| **Singleton** | ❌ No | `CsvExporter` and `ReceiptGenerator` are pure static utility classes – acceptable, but not explicitly a pattern |

---

## A07 – Is the name and description of each class clearly reflecting the played role?
### ✅ YES (mostly)

| Class | Role | Name Clear? | Notes |
|---|---|---|---|
| `DrinkShopService` | Facade over all sub-services | ✅ Yes | |
| `ProductService` | Business logic for products | ✅ Yes | |
| `OrderService` | Business logic for orders | ✅ Yes | |
| `RetetaService` | Business logic for recipes | ✅ Yes | |
| `StocService` | Business logic for stock | ✅ Yes | |
| `DailyReportService` | Revenue/order count for the day | ✅ Yes | |
| `FileAbstractRepository` | Base file-backed repository | ✅ Yes | |
| `FileProductRepository` | File-based product storage | ✅ Yes | |
| `ReceiptGenerator` | Generates receipt string | ⚠️ Partial | Name implies generation but does **not** save – could be `ReceiptFormatter` |
| `CsvExporter` | Exports orders to CSV | ✅ Yes | |
| `IngredientReteta` | Ingredient within a recipe | ⚠️ Partial | Mixed Romanian/English naming style – `IngredientReteta` vs `OrderItem` |
| `DrinkShopApp` | JavaFX app entry point | ✅ Yes | |
| `DrinkShopController` | JavaFX UI controller | ✅ Yes | |
| `ValidationException` | Exception for validation errors | ✅ Yes | |

**Note:** Inconsistent naming language – some classes use Romanian (`Reteta`, `IngredientReteta`, `Stoc`, `TipBautura`, `CategorieBautura`) while others use English (`Order`, `OrderItem`, `Product`).

---

## A08 – Is the description of each class accurately capturing the responsibilities of the class?
### ⚠️ NO (several responsibility issues)

| Issue | Class | Description |
|---|---|---|
| `Order` has a `computeTotalPrice()` method that modifies its own state based on items – **business logic inside domain entity** | `Order.java` line 63 |
| `Order` has both `getTotalPrice()` and `getTotal()` that return the same value – **duplicate methods** | `Order.java` lines 33, 62 |
| `Stoc` has an `isSubMinim()` method – a **business rule** (domain logic) inside an entity | `Stoc.java` line 48 |
| `DrinkShopService` directly calls `ReceiptGenerator.generate()` and `CsvExporter.exportOrders()` (static utility calls) instead of having dedicated service methods or abstractions | `DrinkShopService.java` lines 70, 76 |
| `DrinkShopController` builds `Reteta` IDs with `service.getAllRetete().size()+1` – **ID generation logic in the UI layer** | `DrinkShopController.java` line 186 |
| `DrinkShopController` has `currentOrder = new Order(1)` hardcoded – **order ID management in the UI** | `DrinkShopController.java` line 51 |
| `FileAbstractRepository` has `loadFromFile()` commented out in constructor but called in subclass constructors – **inconsistent lifecycle** | `FileAbstractRepository.java` line 14 |

---

## A09 – Are the role names of aggregations and associations accurately describing the relationship between the related classes?
### ⚠️ NO (partially – visible from class diagram)

| Relationship | From → To | Role Name in Diagram | Accurate? | Notes |
|---|---|---|---|---|
| `DrinkShopController` → `DrinkShopService` | uses | `service` | ✅ Yes | |
| `DrinkShopService` → `OrderService` | delegates | `orderService` (internal) | ✅ Yes | |
| `Order` → `OrderItem` | composition | `items` / `currentOrderItems` | ✅ Yes | |
| `OrderItem` → `Product` | association | `product` | ✅ Yes | |
| `Reteta` → `IngredientReteta` | composition | `ingrediente` | ✅ Yes | |
| `Product` → `CategorieBautura` | association | `categorie` | ✅ Yes | |
| `Product` → `TipBautura` | association | `tip` | ✅ Yes | |
| `DrinkShopService` → `DailyReportService` | uses | `report` | ⚠️ Unclear | Named `report` – could be `dailyReport` for clarity |
| `FileOrderRepository` → `Repository<Integer,Product>` | dependency | `productRepository` | ✅ Yes | but this is a **cross-layer dependency** – a file repository depends on another repository |
| `DrinkShopController` → `Order` | manages | `currentOrder` | ✅ Yes | but the **UI managing Order state** is an architectural concern |

---

## A10 – Are the key entity classes and their relationships consistent with the business model, domain model, and requirements?
### ⚠️ YES (with notable gaps)

| Requirement | Entity | Consistent? | Notes |
|---|---|---|---|
| Product has type and category (CF2) | `Product` has `TipBautura` and `CategorieBautura` | ✅ Yes | |
| Recipe has multiple ingredients with quantities (CF4) | `Reteta` has `List<IngredientReteta>`, each with `denumire` + `cantitate` | ✅ Yes | |
| Order contains products with quantity (CF6) | `Order` has `List<OrderItem>`, each with `Product` + `quantity` | ✅ Yes | |
| Stock management per ingredient (CF5) | `Stoc` has `ingredient` (String), `cantitate`, `stocMinim` | ✅ Yes | |
| CRUD for types and categories (CF3) | `TipBautura` and `CategorieBautura` are **enums** | ❌ No | Enums cannot be managed at runtime; **no entity, repository or service exists** for them |
| Reteta is linked to a Product (CF4+CF7) | `Reteta.id` == `Product.id` (implicit link by ID) | ⚠️ Weak | The relationship is implicit via matching IDs – **no explicit `productId` field or `Product` reference** in `Reteta` |
| Receipt saved as .csv (CF7) | `ReceiptGenerator` returns a `String` | ❌ No | No file saving for individual receipts |
| Client entity (Intro paragraph) | No `Client` class exists | ❌ Missing | Orders have no customer association |

---

## Summary Table

| Nr. | Check Point | Result |
|---|---|---|
| A01 | Overall organization clear | ✅ YES |
| A02 | Package partitioning logically consistent | ✅ YES (minor issues) |
| A03 | Architecture accounts for all requirements | ⚠️ PARTIAL |
| A04 | Classes support subsystem services | ✅ YES (minor gap) |
| A05 | Coherent error handling strategy | ❌ NO |
| A06 | Classic design patterns considered | ✅ YES (partially) |
| A07 | Class names reflect their role | ✅ YES (mostly) |
| A08 | Class responsibilities accurately captured | ⚠️ NO |
| A09 | Aggregation/association role names accurate | ⚠️ PARTIAL |
| A10 | Entity classes consistent with domain model | ⚠️ PARTIAL |
