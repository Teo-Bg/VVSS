package drinkshop.ui;

import drinkshop.domain.*;
import drinkshop.service.DrinkShopService;
import drinkshop.service.validator.ValidationException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class DrinkShopController {

    private DrinkShopService service;

    // ---------- PRODUCT ----------
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> colProdId;
    @FXML private TableColumn<Product, String> colProdName;
    @FXML private TableColumn<Product, Double> colProdPrice;
    @FXML private TableColumn<Product, String> colProdCategorie;
    @FXML private TableColumn<Product, String> colProdTip;
    @FXML private TextField txtProdName, txtProdPrice;
    @FXML private ComboBox<String> comboProdCategorie;
    @FXML private ComboBox<String> comboProdTip;
    @FXML private TextField txtNewCategorie;
    @FXML private TextField txtNewTip;

    // ---------- RETETE ----------
    @FXML private TableView<Reteta> retetaTable;
    @FXML private TableColumn<Reteta, Integer> colRetetaId;
    @FXML private TableColumn<Reteta, String> colRetetaDesc;

    @FXML private TableView<IngredientReteta> newRetetaTable;
    @FXML private TableColumn<IngredientReteta, String> colNewIngredName;
    @FXML private TableColumn<IngredientReteta, Double> colNewIngredCant;
    @FXML private TextField txtNewIngredName, txtNewIngredCant;

    // ---------- ORDER (CURRENT) ----------
    @FXML private TableView<OrderItem> currentOrderTable;
    @FXML private TableColumn<OrderItem, String> colOrderProdName;
    @FXML private TableColumn<OrderItem, Integer> colOrderQty;

    @FXML private ComboBox<Integer> comboQty;
    @FXML private Label lblOrderTotal;
    @FXML private TextArea txtReceipt;

    @FXML private Label lblTotalRevenue;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<Reteta> retetaList = FXCollections.observableArrayList();
    private ObservableList<IngredientReteta> newRetetaList = FXCollections.observableArrayList();
    private ObservableList<OrderItem> currentOrderItems = FXCollections.observableArrayList();

    private Order currentOrder = null;

    public void setService(DrinkShopService service) {
        this.service = service;
        // Start next order ID from max existing ID + 1 to avoid conflicts
        int nextId = service.getAllOrders().stream()
                .mapToInt(Order::getId)
                .max()
                .orElse(0) + 1;
        currentOrder = new Order(nextId);
        initData();
    }

    @FXML
    private void initialize() {

        // PRODUCTS
        colProdId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colProdName.setCellValueFactory(new PropertyValueFactory<>("nume"));
        colProdPrice.setCellValueFactory(new PropertyValueFactory<>("pret"));
        colProdCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colProdTip.setCellValueFactory(new PropertyValueFactory<>("tip"));
        productTable.setItems(productList);

        comboProdCategorie.getItems().setAll(CategorieBautura.values());
        comboProdTip.getItems().setAll(TipBautura.values());

        // RETETE
        colRetetaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRetetaDesc.setCellValueFactory(data -> {
            Reteta r = data.getValue();
            String desc = r.getIngrediente().stream()
                    .map(i -> i.getDenumire() + " (" + i.getCantitate() + ")")
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(desc);
        });
        retetaTable.setItems(retetaList);

        colNewIngredName.setCellValueFactory(new PropertyValueFactory<>("denumire"));
        colNewIngredCant.setCellValueFactory(new PropertyValueFactory<>("cantitate"));
        newRetetaTable.setItems(newRetetaList);

        // CURRENT ORDER TABLE
        colOrderProdName.setCellValueFactory(data -> {
            int prodId = data.getValue().getProduct().getId();
            Product prod = productList.stream().filter(p -> p.getId() == prodId).findFirst().orElse(null);
            return new SimpleStringProperty(prod != null ? prod.getNume() : "N/A");
        });
        colOrderQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        currentOrderTable.setItems(currentOrderItems);

        comboQty.setItems(FXCollections.observableArrayList(1,2,3,4,5,6,7,8,9,10));
    }

    private void initData() {
        productList.setAll(service.getAllProducts());
        retetaList.setAll(service.getAllRetete());
        lblTotalRevenue.setText("Daily Revenue: " + service.getDailyRevenue());
        updateOrderTotal();
    }

    // ---------- PRODUCT ----------
    @FXML
    private void onAddProduct() {
        double pret;
        try {
            pret = Double.parseDouble(txtProdPrice.getText());
        } catch (NumberFormatException ex) {
            showError("Prețul trebuie să fie un număr valid.");
            return;
        }

        int newId = service.getAllProducts().stream()
                .mapToInt(Product::getId)
                .max()
                .orElse(0) + 1;

        Product p = new Product(newId,
                txtProdName.getText(),
                pret,
                comboProdCategorie.getValue(),
                comboProdTip.getValue());
        try {
            service.addProduct(p);
        } catch (ValidationException ex) {
            showError(ex.getMessage());
            return;
        }
        initData();
    }

    @FXML
    private void onUpdateProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        double pret;
        try {
            pret = Double.parseDouble(txtProdPrice.getText());
        } catch (NumberFormatException ex) {
            showError("Prețul trebuie să fie un număr valid.");
            return;
        }

        try {
            service.updateProduct(selected.getId(), txtProdName.getText(),
                    pret,
                    comboProdCategorie.getValue(), comboProdTip.getValue());
        } catch (ValidationException ex) {
            showError(ex.getMessage());
            return;
        }
        initData();
    }

    @FXML
    private void onDeleteProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            service.deleteProduct(selected.getId());
        } catch (ValidationException ex) {
            showError(ex.getMessage());
            return;
        }
        initData();
    }

    @FXML
    private void onFilterCategorie() {
        productList.setAll(service.filtreazaDupaCategorie(comboProdCategorie.getValue()));
    }

    @FXML
    private void onFilterTip() {
        productList.setAll(service.filtreazaDupaTip(comboProdTip.getValue()));
    }

    // ---------- RETETA NOUA ----------
    @FXML
    private void onAddNewIngred() {
        String nume = txtNewIngredName.getText().trim();
        if (nume.isBlank()) {
            showError("Numele ingredientului nu poate fi gol!");
            return;
        }
        double cantitate;
        try {
            cantitate = Double.parseDouble(txtNewIngredCant.getText());
        } catch (NumberFormatException ex) {
            showError("Cantitatea trebuie să fie un număr valid.");
            return;
        }
        newRetetaList.add(new IngredientReteta(nume, cantitate));
    }

    @FXML
    private void onDeleteNewIngred() {
        IngredientReteta sel = newRetetaTable.getSelectionModel().getSelectedItem();
        if (sel != null) newRetetaList.remove(sel);
    }

    @FXML
    private void onAddNewReteta() {
        // Use max existing ID + 1 to avoid conflicts when items were deleted
        int newId = service.getAllRetete().stream()
                .mapToInt(Reteta::getId)
                .max()
                .orElse(0) + 1;
        Reteta r = new Reteta(newId, new ArrayList<>(newRetetaList));
        try {
            service.addReteta(r);
        } catch (ValidationException ex) {
            showError(ex.getMessage());
            return;
        }
        newRetetaList.clear();
        initData();
    }

    @FXML
    private void onClearNewRetetaIngredients() {
        newRetetaTable.getItems().clear();
        txtNewIngredName.clear();
        txtNewIngredCant.clear();
    }

    // ---------- CURRENT ORDER ----------
    @FXML
    private void onAddOrderItem() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        Integer qty = comboQty.getValue();

        if (selected == null) {
            showError("Selectează un produs din listă.");
            return;
        }
        if (qty == null) {
            showError("Selectează cantitatea.");
            return;
        }

        currentOrderItems.add(new OrderItem(selected, qty));
        updateOrderTotal();
    }

    @FXML
    private void onDeleteOrderItem() {
        OrderItem sel = currentOrderTable.getSelectionModel().getSelectedItem();
        if (sel != null) {
            currentOrderItems.remove(sel);
            updateOrderTotal();
        }
    }

    @FXML
    private void onFinalizeOrder() {
        currentOrder.getItems().clear();
        currentOrder.getItems().addAll(currentOrderItems);
        currentOrder.computeTotalPrice();

        try {
            service.addOrder(currentOrder);
        } catch (IllegalStateException ex) {
            showError(ex.getMessage());
            return;
        }

        txtReceipt.setText(service.generateReceipt(currentOrder));

        currentOrderItems.clear();
        currentOrder = new Order(currentOrder.getId() + 1);
        updateOrderTotal();
    }

    private void updateOrderTotal() {
        currentOrder.getItems().clear();
        currentOrder.getItems().addAll(currentOrderItems);
        double total = service.computeTotal(currentOrder);
        lblOrderTotal.setText("Total: " + total);
    }

    // ---------- EXPORT + REVENUE ----------
    @FXML
    private void onExportOrdersCsv() {
        service.exportCsv("orders.csv");
    }

    @FXML
    private void onDailyRevenue() {
        lblTotalRevenue.setText("Daily Revenue: " + service.getDailyRevenue());
    }

    // ---------- CATEGORII / TIPURI ----------
    @FXML
    private void onAddCategorie() {
        String val = txtNewCategorie.getText().trim().toUpperCase();
        if (val.isBlank()) { showError("Introdu o denumire pentru categorie."); return; }
        CategorieBautura.addValue(val);
        comboProdCategorie.getItems().setAll(CategorieBautura.values());
        comboProdCategorie.setValue(val);
        txtNewCategorie.clear();
    }

    @FXML
    private void onAddTip() {
        String val = txtNewTip.getText().trim().toUpperCase();
        if (val.isBlank()) { showError("Introdu o denumire pentru tip."); return; }
        TipBautura.addValue(val);
        comboProdTip.getItems().setAll(TipBautura.values());
        comboProdTip.setValue(val);
        txtNewTip.clear();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }
}