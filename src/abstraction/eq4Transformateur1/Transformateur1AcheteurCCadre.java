package abstraction.eq4Transformateur1;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import abstraction.eqXRomu.contratsCadres.IVendeurContratCadre;
import abstraction.eqXRomu.bourseCacao.BourseCacao;
import abstraction.eqXRomu.contratsCadres.Echeancier;
import abstraction.eqXRomu.contratsCadres.ExemplaireContratCadre;
import abstraction.eqXRomu.contratsCadres.IAcheteurContratCadre;
import abstraction.eqXRomu.contratsCadres.SuperviseurVentesContratCadre;
import abstraction.eqXRomu.filiere.Filiere;
import abstraction.eqXRomu.filiere.IActeur;
import abstraction.eqXRomu.general.Journal;
import abstraction.eqXRomu.general.Variable;
import abstraction.eqXRomu.produits.Chocolat;
import abstraction.eqXRomu.produits.ChocolatDeMarque;
import abstraction.eqXRomu.produits.Feve;
import abstraction.eqXRomu.produits.Gamme;
import abstraction.eqXRomu.produits.IProduit;

/**
 * @author Oscar_Brian 
 */
public class Transformateur1AcheteurCCadre extends Transformateur1AcheteurBourse implements IAcheteurContratCadre {
	private SuperviseurVentesContratCadre supCC;
	private List<ExemplaireContratCadre> contratsTermines;
	protected Journal journalCC;
	
	public Transformateur1AcheteurCCadre() {
		super();
		this.contratsEnCoursAchat=new LinkedList<ExemplaireContratCadre>();
		this.contratsTermines=new LinkedList<ExemplaireContratCadre>();
		this.journalCC = new Journal(this.getNom()+" journal CC acheteur", this);
	}

	/**
	 * A MODIFIER EN FONCTION DES DIFFERENTS CONTRATS QU'ON VEUT FAIRE, CHOISIR QUI 
	 */
	public void initialiser() {
		super.initialiser();
		this.supCC = (SuperviseurVentesContratCadre)(Filiere.LA_FILIERE.getActeur("Sup.CCadre"));
	
	}
	
	// Renvoie les journaux
		public List<Journal> getJournaux() {
			List<Journal> res=super.getJournaux();
			res.add(journalCC);
			return res;
		}
	
	public void next() {
	    super.next();
	    this.journalCC.ajouter("=== STEP " + Filiere.LA_FILIERE.getEtape() + " ====================");
	    for (Feve f : stockFeves.keySet()) {
	        if (achete(f)) {
	            this.journalCC.ajouter("   " + f + " suffisamment peu en stock/contrat pour passer un CC");
	            double parStep = Math.max(100, (this.stockCibleMini*this.listePourcentageMarque.get(f.getGamme()) *1.1 + stockFeves.get(f).getValeur() - restantDu(f)) / 12); // au moins 100
	            Echeancier e = new Echeancier(Filiere.LA_FILIERE.getEtape() + 1, 12, parStep);
	            List<IVendeurContratCadre> vendeurs = supCC.getVendeurs(f);
	            if (!vendeurs.isEmpty()) {
	                IVendeurContratCadre vendeur = vendeurs.get(Filiere.random.nextInt(vendeurs.size()));
	                journalCC.ajouter("   " + vendeur.getNom() + " retenu comme vendeur parmi " + vendeurs.size() + " vendeurs potentiels");
	                ExemplaireContratCadre contrat = supCC.demandeAcheteur(this, vendeur, f, e, cryptogramme, false);
	                if (contrat == null) {
	                    journalCC.ajouter(Color.RED, Color.WHITE, "   échec des négociations");
	                } else {
	                    this.contratsEnCoursAchat.add(contrat);
	                    journalCC.ajouter(Color.GREEN, Color.WHITE, "   contrat signé avec l'échéancier : "+contrat.getEcheancier());
	                }
	            } else {
	                journalCC.ajouter("   pas de vendeur");
	            }
	        }
	    }
	    // On archive les contrats terminés
	    for (ExemplaireContratCadre c : new ArrayList<>(this.contratsEnCoursAchat)) {
	        if ((c.getQuantiteRestantALivrer() == 0.0 && c.getMontantRestantARegler() <= 0.0) || (Filiere.LA_FILIERE.getActeursSolvables().contains(c.getAcheteur()) && Filiere.LA_FILIERE.getActeursSolvables().contains(c.getVendeur()))) {
	            this.contratsTermines.add(c);
	            this.contratsEnCoursAchat.remove(c);
		        journalCC.ajouter("Archivage du contrat " + c);
	        }
	    }
	    
	    this.journalCC.ajouter("=================================");}
	    
	public double restantDu(Feve f) {
		double res=0;
		for (ExemplaireContratCadre c : this.contratsEnCoursAchat) {
			if (c.getProduit().equals(f)) { 
				res+=c.getQuantiteRestantALivrer();
			}
		}
		return res;
	}
	
	public double restantAPayer() {
		double res=0;
		for (ExemplaireContratCadre c : this.contratsEnCoursAchat) {
			res+=c.getMontantRestantARegler();
		}
		return res;
		}
	
	private double stockChocoTransformer(Feve feve) {
		double stockChocoTransformer = 0;
		for (ChocolatDeMarque cdm : this.stockChocoMarque.keySet()) {
			if(cdm.getGamme() == feve.getGamme()) {
				stockChocoTransformer += this.stockChocoMarque.get(cdm).getValeur();
			}
		}
		return stockChocoTransformer;
	}
	
	@Override
	public boolean achete(IProduit produit) {
		if (produit instanceof Feve) {
	        Feve feve = (Feve) produit;
	        if (feve.getType().equals("Feve")) {
	        	if ((feve.getGamme() == Gamme.HQ && feve.isBio() && feve.isEquitable()) || feve.getGamme() == Gamme.MQ) {
	        		boolean reponse = stockChocoTransformer(feve) + stockFeves.get(feve).getValeur() + restantDu(feve) - this.stockCibleMini*this.listePourcentageMarque.get(feve.getGamme()) <= Math.max(this.demandeCC.get(feve.getGamme()) * nombreMois, this.quantiteMiniCC);
	    	    	journalCC.ajouter("La feve proposée : "+feve+", réponse : "+reponse);
	        		return reponse;
	        	}
        	}
	    }
	    return false;
	}
	            
		


	@Override
	/**
	 * @author Yannig_charonnat
	 */
	public Echeancier contrePropositionDeLAcheteur(ExemplaireContratCadre contrat) {
		if (!(contrat.getProduit() instanceof Feve)) {
	        return null;
	    }

	    Feve feve = (Feve) contrat.getProduit();
	    if (!feve.getType().equals("Feve")) {
	        return null;
	    }
	    
	    if(!achete(contrat.getProduit())) {
	    	return null;
	    }
	    
	    if((feve.getGamme() == Gamme.HQ && feve.isBio() && feve.isEquitable()) || feve.getGamme() == Gamme.MQ) {
	    	if(contrat.getQuantiteTotale() <= this.stockCibleMini*this.listePourcentageMarque.get(feve.getGamme()) - stockChocoTransformer(feve) - stockFeves.get(feve).getValeur() - restantDu(feve)) {
	    		return contrat.getEcheancier();
	    	} else {
	    		double quantiteTotale = this.stockCibleMini*this.listePourcentageMarque.get(feve.getGamme()) - stockChocoTransformer(feve) - stockFeves.get(feve).getValeur() - restantDu(feve);
	    		Echeancier echeancier = new Echeancier(Filiere.LA_FILIERE.getEtape()+1, 12, Math.max(quantiteTotale, this.quantiteMiniCC) / 12);
	    		return echeancier;
	    	}
	    }
	    
		return null;
	}
	
	@Override
	public double contrePropositionPrixAcheteur(ExemplaireContratCadre contrat) {
	    if (!(contrat.getProduit() instanceof Feve)) {
	        return 0.0;
	    }

	    Feve feve = (Feve) contrat.getProduit();
	    if (!feve.getType().equals("Feve")) {
	        return 0.0;
	    }

	    if (achete(feve)) {
	        BourseCacao bourse = (BourseCacao)(Filiere.LA_FILIERE.getActeur("BourseCacao"));
	        double solde = Filiere.LA_FILIERE.getBanque().getSolde(this, cryptogramme) - restantAPayer();
	        double prixSansDecouvert = solde / contrat.getQuantiteTotale();
	        if (prixSansDecouvert < bourse.getCours(Feve.F_BQ).getValeur()) {
	            return 0.0; // Nous ne sommes pas en mesure de fournir un prix raisonnable
	        }
	        if (feve.isEquitable()) { // Pas de cours en bourse
	            double max = bourse.getCours(Feve.F_MQ).getMax() * 1.25;
	            double alea = Filiere.random.nextInt((int) max);
	            if (contrat.getPrix() < Math.min(alea, prixSansDecouvert)) {
	                return contrat.getPrix();
	            } else {
	                return Math.min(prixSansDecouvert, bourse.getCours(Feve.get(feve.getGamme(), false, false)).getValeur() * (1 + (Filiere.random.nextInt(25) / 100.0))); // Entre 1 et 1.25 le prix de la feve
	            }
	        } else {
	            double cours = bourse.getCours(feve).getValeur();
	            double coursMax = bourse.getCours(feve).getMax();
	            int alea = coursMax - cours > 1 ? Filiere.random.nextInt((int) (coursMax - cours)) : 0;
	            if (contrat.getPrix() < cours + alea) {
	                return Math.min(prixSansDecouvert, contrat.getPrix());
	            } else {
	                return Math.min(prixSansDecouvert, cours * (1.1 - (Filiere.random.nextDouble() / 3.0)));
	            }
	        }
	    } else {
	        return 0.0; // Ne pas faire de proposition de prix si on ne souhaite pas acheter la fève
	    }}

	@Override
	public void notificationNouveauContratCadre(ExemplaireContratCadre contrat) {
		journalCC.ajouter(Color.GREEN, Color.WHITE, "contrat accepté : "+"#"+contrat.getNumero()+" | acheteur : "+contrat.getAcheteur()+" | vendeur : "+contrat.getVendeur()+" | produit : "+contrat.getProduit()+" | quaantité totale : "+contrat.getQuantiteTotale()+" | Prix : "+contrat.getPrix());	
		this.contratsEnCoursAchat.add(contrat);
	}

	@Override
	public void receptionner(IProduit p, double quantiteEnTonnes, ExemplaireContratCadre contrat) {
		journalCC.ajouter("Reception de "+quantiteEnTonnes+" T de "+p+" du contrat "+contrat.getNumero());
		stockFeves.get((Feve)p).setValeur(this, stockFeves.get((Feve)p).getValeur()+quantiteEnTonnes);
		totalStocksFeves.ajouter(this, quantiteEnTonnes, cryptogramme);
	}
}
	

