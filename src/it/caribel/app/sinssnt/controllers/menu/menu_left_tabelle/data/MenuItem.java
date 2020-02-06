package it.caribel.app.sinssnt.controllers.menu.menu_left_tabelle.data;

public class MenuItem extends it.caribel.zk.generic_controllers.menu_tree.data.pojo.MenuItem {
	
	public MenuItem(String label) {
		super(label);
	}
	
	/* (non-Javadoc)
	 * @see it.caribel.zk.generic_controllers.menu_tree.data.pojo.MenuItem#setKeyPermission(java.lang.String)
	 * Come indicato da Roberto, per le tabelle di contorno, mostro solo le voci di menu
	 * per cui l'operatore ha almeno i permessi di UPDATE/INSE/CANC.
	 * Se l'operatore ha solo il permesso CONS non mostro la voce di menu perche
	 * significherebbe mostrare la lista senza poterci fare nulla!
	 */
	@Override
	public void setKeyPermission(String keyPermission) {
		super.keyPermission = keyPermission;
		if(keyPermission!=null && !keyPermission.trim().equals(""))
			if(!iu.canIUse(keyPermission) || canIUseOnlyCONS(keyPermission)){
				this.enabled = false;
		}
	}
	
	@Override
	public void setKeyPermissionChildrens(String[] keyPermissionChildrens) {
		this.keyPermissionChildrens = keyPermissionChildrens;
		boolean trovatoFiglioEnabled = false;
		if(keyPermissionChildrens!=null){
			String corrKey = "";
			for(int i=0; i<keyPermissionChildrens.length;i++){
				corrKey = keyPermissionChildrens[i];
				if(corrKey.trim().equals("") || (iu.canIUse(corrKey) && !canIUseOnlyCONS(corrKey))){
					trovatoFiglioEnabled = true;
				}
			}
			if(!trovatoFiglioEnabled){
				this.enabled = false;//allora disabilito anche il nodo padre
			}
		}
	}

	private boolean canIUseOnlyCONS(String keyPermission){
		if(iu.canIUse(keyPermission,"CONS") && !iu.canIUse(keyPermission,"MODI") && !iu.canIUse(keyPermission,"CANC") && !iu.canIUse(keyPermission,"INSE"))
			return true;
		else
			return false;
	}
	
	

	
}
