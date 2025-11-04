import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet, RouterLink } from '@angular/router';
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

  // Auth state
  isLoggedIn = computed(() => this.authService.isAuthenticated());
  userName = computed(() => {
    const user = this.authService.currentUser();
    return user ? `${user.firstName} ${user.lastName}` : 'User';
  });

  // Cart state
  cartItemCount = computed(() => this.cartService.cartItemCount());

  // Theme state
  theme = signal<'light' | 'dark'>((localStorage.getItem('theme') as any) || 'dark');

  constructor() {
    // Apply saved theme on bootstrap
    document.body.dataset['theme'] = this.theme();
    // Init custom cursor movement
    this.installCursorGlow();
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

  toggleTheme(): void {
    const next = this.theme() === 'light' ? 'dark' : 'light';
    this.theme.set(next);
    localStorage.setItem('theme', next);
    document.body.dataset['theme'] = next;
  }

  private installCursorGlow(): void {
    const glow = document.getElementById('cursor-glow');
    if (!glow) return;
    const move = (e: MouseEvent) => {
      const x = e.clientX;
      const y = e.clientY;
      glow!.setAttribute('style', `transform: translate(${x - 15}px, ${y - 15}px)`);
    };
    window.addEventListener('mousemove', move);
  }
}
