package drinkshop.domain;

public class Product {

    private int id;
    private String nume;
    private double pret;
    private String categorie;
    private String tip;

    public Product(int id, String nume, double pret,
                  String categorie, String tip) {
        this.id = id;
        this.nume = nume;
        this.pret = pret;
        this.categorie = categorie;
        this.tip = tip;
    }

    public int getId() { return id; }
    public String getNume() { return nume; }
    public double getPret() { return pret; }
    public String getCategorie() { return categorie; }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public String getTip() { return tip; }

    public void setTip(String tip) {
        this.tip = tip;
    }
    public void setNume(String nume) { this.nume = nume; }
    public void setPret(double pret) { this.pret = pret; }

    @Override
    public String toString() {
        return nume + " (" + categorie + ", " + tip + ") - " + pret + " lei";
    }
}