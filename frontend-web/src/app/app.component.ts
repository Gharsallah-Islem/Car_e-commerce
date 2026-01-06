import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet, RouterLink, RouterLinkActive, NavigationEnd } from '@angular/router';
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
    RouterLinkActive,
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

  // UI State
  isScrolled = signal(false);
  mobileMenuOpen = signal(false);
  searchOpen = signal(false);

  constructor() {
    // Check initial route
    const currentUrl = this.router.url;
    this.showNavbar.set(!currentUrl.includes('/admin') && !currentUrl.includes('/support'));

    // Listen to route changes
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      const url = event.url;
      this.showNavbar.set(!url.includes('/admin') && !url.includes('/support'));
      this.mobileMenuOpen.set(false); // Close mobile menu on route change
      this.searchOpen.set(false); // Close search on route change
    });

    // Add scroll listener
    if (typeof window !== 'undefined') {
      window.addEventListener('scroll', () => {
        this.isScrolled.set(window.scrollY > 20);
      });
    }
  }

  toggleMobileMenu(): void {
    this.mobileMenuOpen.update(v => !v);
    if (this.mobileMenuOpen()) {
      this.searchOpen.set(false);
    }
  }

  toggleSearch(): void {
    this.searchOpen.update(v => !v);
    if (this.searchOpen()) {
      this.mobileMenuOpen.set(false);
      // Focus input after a small delay to allow DOM to render
      setTimeout(() => {
        const input = document.querySelector('.search-modal input') as HTMLElement;
        if (input) input.focus();
      }, 100);
    }
  }

  onSearch(): void {
    const query = this.searchQuery();
    if (query.trim()) {
      this.router.navigate(['/products'], { queryParams: { search: query } });
      this.searchOpen.set(false);
    }
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/']);
    this.mobileMenuOpen.set(false);
  }
}
