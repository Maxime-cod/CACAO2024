package abstraction.eq2Producteur2;

import abstraction.eqXRomu.produits.*;
import abstraction.eqXRomu.filiere.*;

public class Producteur2_Lot {
	
	private int etape;
	private double quantite;
	private Feve type_feve;
	
	public Producteur2_Lot(double quantite, Feve type_feve) {
		this.etape = Filiere.LA_FILIERE.getEtape();
		this.quantite = quantite;
		this.type_feve = type_feve;
	}
	
	public Producteur2_Lot(double quantite, Feve type_feve, int etape) {
		this.etape = etape;
		this.quantite = quantite;
		this.type_feve = type_feve;
	}
	
	public int getEtape() {
		return etape;
	}

	public void setEtape(int etape) {
		this.etape = etape;
	}

	public double getQuantite() {
		return quantite;
	}

	public void setQuantite(double quantite) {
		this.quantite = quantite;
	}

	public Feve getType_feve() {
		return type_feve;
	}

	public void setType_feve(Feve type_feve) {
		this.type_feve = type_feve;
	}	
}


