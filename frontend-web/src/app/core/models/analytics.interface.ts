export interface DashboardStats {
    totalOrders: number;
    totalRevenue: number;
    totalProducts: number;
    totalUsers: number;
    pendingOrders: number;
    lowStockProducts: number;
    todayOrders: number;
    todayRevenue: number;
    activeUsers: number;
    pendingReclamations: number;
    averageOrderValue: number;
    conversionRate: number;
    ordersGrowth?: number;
    revenueGrowth?: number;
    usersGrowth?: number;
    productsGrowth?: number;
}

export interface SalesChartData {
    date: string;
    revenue: number;
    orders: number;
}

export interface CategoryPerformance {
    categoryName: string;
    productCount: number;
    orderCount: number;
    revenue: number;
    percentage: number;
}

export interface TopProduct {
    productId: string;
    productName: string;
    category: string;
    brand: string;
    unitsSold: number;
    revenue: number;
    imageUrl: string;
}

export interface RevenueByPeriod {
    periodType: string;
    startDate: string;
    endDate: string;
    totalRevenue: number;
    totalOrders: number;
    averageOrderValue: number;
}

export interface CustomerAnalytics {
    totalCustomers: number;
    newCustomersToday: number;
    newCustomersThisWeek: number;
    newCustomersThisMonth: number;
    activeCustomers: number;
    returningCustomers: number;
    retentionRate: number;
    customerLifetimeValue: number;
}

export interface OrderStatusDistribution {
    status: string;
    count: number;
    percentage: number;
}

export interface RecentActivity {
    type: string;
    description: string;
    timestamp: string;
    userId: string;
    userName: string;
    metadata: { [key: string]: string };
}

export interface ProductInventoryAlert {
    productId: string;
    productName: string;
    category: string;
    currentStock: number;
    minimumStock: number;
    severity: 'CRITICAL' | 'WARNING' | 'INFO';
    lastUpdated: string;
}

export interface ComprehensiveAnalytics {
    dashboardStats: DashboardStats;
    salesChartData: SalesChartData[];
    categoryPerformance: CategoryPerformance[];
    topProducts: TopProduct[];
    orderStatusDistribution: OrderStatusDistribution[];
    customerAnalytics: CustomerAnalytics;
    recentActivities: RecentActivity[];
    inventoryAlerts: ProductInventoryAlert[];
}
