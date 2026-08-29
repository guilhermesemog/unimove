import { Component, computed, input } from '@angular/core';
import { NgClass } from '@angular/common';

type StatusTone = 'neutral' | 'positive' | 'warning' | 'danger' | 'info';

const STATUS_CONFIG: Record<string, { label: string; tone: StatusTone }> = {
  OPEN: { label: 'Open', tone: 'positive' },
  ACTIVE: { label: 'Active', tone: 'positive' },
  APPROVED: { label: 'Approved', tone: 'positive' },
  COMPLETED: { label: 'Completed', tone: 'positive' },
  PROCESSING: { label: 'Planning', tone: 'warning' },
  PENDING: { label: 'Pending', tone: 'warning' },
  SCHEDULED: { label: 'Confirmed', tone: 'info' },
  STARTED: { label: 'In Progress', tone: 'info' },
  CLOSED: { label: 'Closed', tone: 'neutral' },
  INACTIVE: { label: 'Inactive', tone: 'neutral' },
  REJECTED: { label: 'Rejected', tone: 'danger' },
  CANCELLED: { label: 'Cancelled', tone: 'danger' },
};

@Component({
  selector: 'app-status-chip',
  standalone: true,
  imports: [NgClass],
  templateUrl: './status-chip.component.html',
})
export class StatusChipComponent {
  readonly status = input.required<string>();
  readonly label = input<string>();

  protected readonly config = computed(() => {
    const normalized = this.status().toUpperCase();
    return STATUS_CONFIG[normalized] ?? {
      label: normalized.replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase()),
      tone: 'neutral' as StatusTone,
    };
  });

  protected readonly classes = computed(() => ({
    neutral: 'bg-slate-100 text-slate-700 ring-slate-200',
    positive: 'bg-emerald-50 text-emerald-700 ring-emerald-200',
    warning: 'bg-amber-50 text-amber-800 ring-amber-200',
    danger: 'bg-red-50 text-red-700 ring-red-200',
    info: 'bg-sky-50 text-sky-700 ring-sky-200',
  })[this.config().tone]);

  protected readonly dotClasses = computed(() => ({
    neutral: 'bg-slate-400',
    positive: 'bg-emerald-500',
    warning: 'bg-amber-500',
    danger: 'bg-red-500',
    info: 'bg-sky-500',
  })[this.config().tone]);
}
