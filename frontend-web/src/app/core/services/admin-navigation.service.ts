import { Injectable, signal } from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class AdminNavigationService {
    // Signal to track the selected tab index
    selectedTabIndex = signal<number>(0);

    // Tab mapping
    private tabMap: { [key: string]: number } = {
        'analytics': 0,
        'inventory': 1,
        'delivery': 2,
        'products': 3,
        'orders': 4,
        'users': 5
    };

    navigateToTab(tabName: string): void {
        const index = this.tabMap[tabName];
        if (index !== undefined) {
            this.selectedTabIndex.set(index);
        }
    }

    setTabIndex(index: number): void {
        this.selectedTabIndex.set(index);
    }
}
