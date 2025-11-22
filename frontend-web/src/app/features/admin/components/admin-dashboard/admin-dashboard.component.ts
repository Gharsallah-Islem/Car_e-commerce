import { Component } from '@angular/core';
import { AnalyticsDashboardComponent } from '../../analytics-dashboard/analytics-dashboard.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [AnalyticsDashboardComponent],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss'
})
export class AdminDashboardComponent {

}
