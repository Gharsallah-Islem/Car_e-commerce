import { Component } from '@angular/core';

@Component({
  selector: 'app-categories',
  templateUrl: './categories.component.html',
  styleUrls: ['./categories.component.scss']
})
export class CategoriesComponent {
  categories = [
    { id: 'freinage', title: 'Freinage', subtitle: 'Plaquettes, disques, étriers', icon: 'https://images.unsplash.com/photo-1515879218367-8466d910aaa4?auto=format&fit=crop&w=800&q=80' },
    { id: 'filtres', title: 'Filtres', subtitle: 'Huile, air, carburant', icon: 'https://images.unsplash.com/photo-1542367597-15171d1d75a1?auto=format&fit=crop&w=800&q=80' },
    { id: 'electrique', title: 'Électrique', subtitle: 'Batteries, alternateurs', icon: 'https://images.unsplash.com/photo-1616633380057-9b9b8b8e1f6b?auto=format&fit=crop&w=800&q=80' },
    { id: 'suspension', title: 'Suspension', subtitle: 'Amortisseurs, ressorts', icon: 'https://images.unsplash.com/photo-1600891964599-f61ba0e24092?auto=format&fit=crop&w=800&q=80' },
    { id: 'eclairage', title: 'Éclairage', subtitle: 'Phares, ampoules LED', icon: 'https://images.unsplash.com/photo-1518166340897-15e4b3e1a9db?auto=format&fit=crop&w=800&q=80' },
    { id: 'allumage', title: "Allumage", subtitle: "Bougies, bobines d'allumage", icon: 'https://images.unsplash.com/photo-1541534401786-2b6f1b3a7f73?auto=format&fit=crop&w=800&q=80' }
  ];
}