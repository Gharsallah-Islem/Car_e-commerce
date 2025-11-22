import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet, RouterLink, NavigationEnd } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatBadgeModule } from '@angular/material/badge';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { LoadingService } from './core/services/loading.service';
import { AuthService } from './core/services/auth.service';
import { CartService } from './core/services/cart.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    FormsModule,
    MatProgressSpinnerModule,
    MatToolbarModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatBadgeModule,
    MatMenuModule,
    MatDividerModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'frontend-web';

  // Services
  loadingService = inject(LoadingService);
  authService = inject(AuthService); // Made public for template access
  private cartService = inject(CartService);
  private router = inject(Router);

  // Search
  searchQuery = signal('');

  // Hide navbar on admin routes
  showNavbar = signal(true);

  // Auth state
  isLoggedIn = computed(() => this.authService.isAuthenticated());
  userName = computed(() => {
    const user = this.authService.currentUser();
    return user ? `${user.firstName} ${user.lastName}` : 'User';
  });

  // Cart state
  cartItemCount = computed(() => this.cartService.cartItemCount());

  constructor() {
    // Check initial route
    const currentUrl = this.router.url;
    this.showNavbar.set(!currentUrl.includes('/admin'));

    // Listen to route changes to hide/show navbar
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      const url = event.url;
      this.showNavbar.set(!url.includes('/admin'));
    });
  }

  onSearch(): void {
    const query = this.searchQuery();
    if (query.trim()) {
      this.router.navigate(['/products'], { queryParams: { search: query } });
    }
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
