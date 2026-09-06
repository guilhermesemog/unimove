import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { HeaderComponent } from '../../../shared/components/list-header/header.component';
import { UiStateComponent } from '../../../shared/components/ui-state/ui-state.component';
import { AnalyticsMetric, AnalyticsTrendPoint, OperationalAnalytics } from '../../../shared/types/analytics.type';
import { AnalyticsRequest, AnalyticsService } from './analytics.service';

type Preset = 7 | 30 | 90;

@Component({
  selector: 'app-analytics',
  imports: [DatePipe, DecimalPipe, FormsModule, HeaderComponent, UiStateComponent],
  templateUrl: './analytics.page.html',
})
export class AnalyticsPage {
  private readonly service = inject(AnalyticsService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly data = signal<OperationalAnalytics | null>(null);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly selectedPreset = signal<Preset | null>(30);
  protected readonly summaryCards = computed(() => {
    const summary = this.data()?.summary;
    return summary ? [
      { label: 'Confirmed bookings', metric: summary.confirmedBookings },
      { label: 'Cancellation rate', metric: summary.cancellationRate },
      { label: 'Capacity utilization', metric: summary.capacityUtilization },
      { label: 'Planning lead time', metric: summary.planningLeadTime },
      { label: 'Assignment lead time', metric: summary.assignmentLeadTime },
      { label: 'Trips at risk', metric: summary.tripsAtRisk },
    ] : [];
  });
  protected readonly chartMaximum = computed(() => Math.max(1, ...this.data()?.trend.flatMap((point) =>
    [point.bookingsCreated, point.bookingsCancelled, point.tripsCreated]) ?? [1]));
  protected readonly hasActivity = computed(() => {
    const value = this.data();
    if (!value) return false;
    return value.trend.some((point) => point.bookingsCreated || point.bookingsCancelled || point.tripsCreated)
      || value.universities.length > 0 || value.recurrence.generatedOccurrences.value !== 0;
  });

  protected fromDate = '';
  protected toDate = '';

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;
    const from = params.get('from');
    const to = params.get('to');
    const days = Number(params.get('days') ?? 30);
    if (from && to) {
      this.fromDate = from;
      this.toDate = to;
      this.selectedPreset.set(null);
      this.fetch({ from, to });
      return;
    }
    const preset: Preset = days === 7 || days === 90 ? days : 30;
    this.selectPreset(preset);
  }

  protected selectPreset(days: Preset): void {
    this.selectedPreset.set(days);
    this.fromDate = '';
    this.toDate = '';
    this.syncUrl({ days });
    this.fetch({ days });
  }

  protected applyCustomPeriod(): void {
    if (!this.fromDate || !this.toDate || this.fromDate > this.toDate) {
      this.error.set('Choose a valid start and end date.');
      return;
    }
    this.selectedPreset.set(null);
    const request = { from: this.fromDate, to: this.toDate };
    this.syncUrl(request);
    this.fetch(request);
  }

  protected retry(): void {
    const preset = this.selectedPreset();
    this.fetch(preset ? { days: preset } : { from: this.fromDate, to: this.toDate });
  }

  protected metricValue(metric: AnalyticsMetric): string {
    if (metric.value === null) return '—';
    const value = metric.value.toLocaleString('en-US', { maximumFractionDigits: 2 });
    if (metric.unit === 'percent') return `${value}%`;
    if (metric.unit === 'hours') return `${value} h`;
    return value;
  }

  protected changeLabel(metric: AnalyticsMetric): string {
    if (metric.changePercent === null) return 'No comparable baseline';
    const sign = metric.changePercent > 0 ? '+' : '';
    return `${sign}${metric.changePercent.toLocaleString('en-US', { maximumFractionDigits: 2 })}% vs previous period`;
  }

  protected changeClass(metric: AnalyticsMetric): string {
    if (metric.changePercent === null || metric.changePercent === 0) return 'text-slate-500';
    return metric.changePercent > 0 ? 'text-brand-700' : 'text-slate-600';
  }

  protected chartHeight(value: number): number {
    return Math.max(value === 0 ? 0 : 3, value / this.chartMaximum() * 100);
  }

  protected showDateLabel(index: number, total: number): boolean {
    return index === 0 || index === total - 1 || index % Math.max(1, Math.ceil(total / 6)) === 0;
  }

  protected pointTitle(point: AnalyticsTrendPoint): string {
    return `${point.date}: ${point.bookingsCreated} bookings, ${point.bookingsCancelled} cancellations, ${point.tripsCreated} trips`;
  }

  private fetch(request: AnalyticsRequest): void {
    this.loading.set(true);
    this.error.set(null);
    this.service.get(request).subscribe({
      next: (response) => { this.data.set(response); this.loading.set(false); },
      error: () => { this.error.set('We could not load operational analytics. Check the period and try again.'); this.loading.set(false); },
    });
  }

  private syncUrl(request: AnalyticsRequest): void {
    this.router.navigate([], { relativeTo: this.route, replaceUrl: true,
      queryParams: 'days' in request ? { days: request.days, from: null, to: null }
        : { days: null, from: request.from, to: request.to } });
  }
}
