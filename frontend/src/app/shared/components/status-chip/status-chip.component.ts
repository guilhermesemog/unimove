import { Component, computed, input } from '@angular/core';
import { NgClass } from '@angular/common';
import { Icon } from '../icon/icon';

type StatusTone = 'neutral' | 'positive' | 'warning' | 'danger' | 'info';

const STATUS_CONFIG: Record<string, { label: string; tone: StatusTone; icon: string }> = {
  OPEN: { label: 'Open', tone: 'positive', icon: 'check_circle' },
  ACTIVE: { label: 'Active', tone: 'positive', icon: 'check_circle' },
  APPROVED: { label: 'Approved', tone: 'positive', icon: 'check_circle' },
  COMPLETED: { label: 'Completed', tone: 'positive', icon: 'task_alt' },
  PROCESSING: { label: 'Planning', tone: 'warning', icon: 'schedule' },
  PENDING: { label: 'Pending', tone: 'warning', icon: 'schedule' },
  SCHEDULED: { label: 'Confirmed', tone: 'info', icon: 'event_available' },
  STARTED: { label: 'In Progress', tone: 'info', icon: 'route' },
  CLOSED: { label: 'Closed', tone: 'neutral', icon: 'lock' },
  INACTIVE: { label: 'Inactive', tone: 'neutral', icon: 'pause_circle' },
  REJECTED: { label: 'Rejected', tone: 'danger', icon: 'cancel' },
  CANCELLED: { label: 'Cancelled', tone: 'danger', icon: 'cancel' },
};

@Component({
  selector: 'app-status-chip',
  standalone: true,
  imports: [NgClass, Icon],
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
      icon: 'info',
    };
  });

  protected readonly classes = computed(() => ({
    neutral: 'bg-slate-100 text-slate-700 ring-slate-200',
    positive: 'bg-emerald-50 text-emerald-700 ring-emerald-200',
    warning: 'bg-amber-50 text-amber-800 ring-amber-200',
    danger: 'bg-red-50 text-red-700 ring-red-200',
    info: 'bg-sky-50 text-sky-700 ring-sky-200',
  })[this.config().tone]);
}
