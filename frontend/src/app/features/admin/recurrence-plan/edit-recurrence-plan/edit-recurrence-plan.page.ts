import { Component, Input, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { switchMap } from 'rxjs';

import { HeaderComponent } from '../../../../shared/components/list-header/header.component';
import { TelemetryService } from '../../../../core/telemetry/telemetry.service';
import { UiStateComponent } from '../../../../shared/components/ui-state/ui-state.component';
import { InterestList } from '../../../../shared/types/interest-list.type';
import { DayOfWeek, RecurrencePlan, RecurrencePlanRequest, RecurrencePreview } from '../../../../shared/types/recurrence-plan.type';
import { University } from '../../../../shared/types/university.type';
import { InterestListService } from '../../interest-list/interest-list.service';
import { UniversityService } from '../../university/university.service';
import { RecurrencePlanService } from '../recurrence-plan.service';

@Component({
  selector: 'app-edit-recurrence-plan',
  imports: [ReactiveFormsModule, HeaderComponent, UiStateComponent],
  templateUrl: './edit-recurrence-plan.page.html',
})
export class EditRecurrencePlanPage {
  @Input() id?: string;
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(RecurrencePlanService);
  private readonly universityService = inject(UniversityService);
  private readonly demandService = inject(InterestListService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly telemetry = inject(TelemetryService);

  protected readonly step = signal(1);
  protected readonly universities = signal<University[]>([]);
  protected readonly selectedDays = signal<DayOfWeek[]>([]);
  protected readonly preview = signal<RecurrencePreview | null>(null);
  protected readonly loading = signal(false);
  protected readonly saving = signal(false);
  protected readonly persistedPlanId = signal<string | null>(null);
  protected readonly error = signal<string | null>(null);
  protected readonly days: { value: DayOfWeek; label: string }[] = [
    { value: 'MONDAY', label: 'Mon' }, { value: 'TUESDAY', label: 'Tue' },
    { value: 'WEDNESDAY', label: 'Wed' }, { value: 'THURSDAY', label: 'Thu' },
    { value: 'FRIDAY', label: 'Fri' }, { value: 'SATURDAY', label: 'Sat' },
    { value: 'SUNDAY', label: 'Sun' },
  ];

  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    destinationId: ['', Validators.required],
    startDate: [this.today(), Validators.required],
    endDate: [this.weeksFromToday(12), Validators.required],
    closingTime: ['16:00', Validators.required],
    departureTime: ['17:30', Validators.required],
    arrivalTime: ['19:00', Validators.required],
    returnDepartureTime: ['23:00', Validators.required],
    returnArrivalTime: ['00:30', Validators.required],
    horizonWeeks: [8, [Validators.required, Validators.min(1), Validators.max(52)]],
  });

  ngOnInit(): void {
    this.telemetry.startFlow('recurrence');
    this.telemetry.track('recurrence_started', { screen: 'recurrence_editor' });
    this.universityService.getUniversitiesAsList().subscribe((items) => this.universities.set(items));
    if (this.id) {
      this.loading.set(true);
      this.service.getById(this.id).subscribe({
        next: (plan) => this.populatePlan(plan),
        error: () => { this.error.set('We could not load this recurring plan.'); this.loading.set(false); },
        complete: () => this.loading.set(false),
      });
      return;
    }
    const sourceId = this.route.snapshot.queryParamMap.get('sourceDemandId');
    if (sourceId) this.demandService.getInterestListById(sourceId).subscribe((demand) => this.populateDemand(demand));
  }

  protected toggleDay(day: DayOfWeek): void {
    this.selectedDays.update((current) => current.includes(day) ? current.filter((item) => item !== day) : [...current, day]);
    this.preview.set(null);
  }

  protected next(): void {
    this.error.set(null);
    if (this.step() < 3) { this.step.update((value) => value + 1); return; }
    if (!this.validRequest()) return;
    this.loading.set(true);
    this.service.preview(this.request()).subscribe({
      next: (result) => {
        this.preview.set(result);
        this.step.set(4);
        this.telemetry.track('recurrence_previewed', { screen: 'recurrence_editor',
          durationMs: this.telemetry.elapsed('recurrence') });
        if (result.conflictCount > 0)
          this.telemetry.track('recurrence_conflict_found', { screen: 'recurrence_editor', result: 'conflict' });
      },
      error: () => { this.error.set('We could not calculate the preview. Check the dates and try again.'); this.loading.set(false); },
      complete: () => this.loading.set(false),
    });
  }

  protected back(): void { if (this.step() > 1) this.step.update((value) => value - 1); }

  protected publish(): void {
    if (!this.preview() || !this.validRequest()) return;
    this.saving.set(true);
    const planId = this.id ?? this.persistedPlanId();
    const save = planId ? this.service.update(planId, this.request()) : this.service.create(this.request());
    save.pipe(switchMap((plan) => {
      this.persistedPlanId.set(plan.id);
      return this.service.generate(plan.id);
    })).subscribe({
      next: (result) => {
        this.telemetry.track('recurrence_published', { screen: 'recurrence_editor', result: 'success',
          durationMs: this.telemetry.elapsed('recurrence', true) });
        this.telemetry.flush();
        this.router.navigate(['/admin/recurrence-plans'], { state: { generation: result } });
      },
      error: () => {
        this.error.set(this.persistedPlanId()
          ? 'The plan was saved, but its demand batch failed. Review and retry safely.'
          : 'The plan could not be saved. Try again.');
        this.saving.set(false);
      },
      complete: () => this.saving.set(false),
    });
  }

  protected actionClass(action: string): string {
    if (action === 'CREATE') return 'bg-brand-100 text-brand-800';
    if (action === 'CONFLICT') return 'bg-amber-100 text-amber-800';
    return 'bg-slate-100 text-slate-600';
  }

  private validRequest(): boolean {
    this.form.markAllAsTouched();
    if (this.form.invalid) { this.error.set('Complete all required fields before continuing.'); return false; }
    if (!this.selectedDays().length) { this.error.set('Select at least one day of the week.'); return false; }
    if (this.form.controls.endDate.value < this.form.controls.startDate.value) { this.error.set('End date must be on or after start date.'); return false; }
    return true;
  }

  private request(): RecurrencePlanRequest {
    return { ...this.form.getRawValue(), daysOfWeek: this.selectedDays() };
  }

  private populatePlan(plan: RecurrencePlan): void {
    this.persistedPlanId.set(plan.id);
    this.form.setValue({
      name: plan.name, destinationId: plan.destination.id, startDate: plan.startDate, endDate: plan.endDate,
      closingTime: plan.closingTime, departureTime: plan.departureTime, arrivalTime: plan.arrivalTime,
      returnDepartureTime: plan.returnDepartureTime, returnArrivalTime: plan.returnArrivalTime,
      horizonWeeks: plan.horizonWeeks,
    });
    this.selectedDays.set([...plan.daysOfWeek]);
  }

  private populateDemand(demand: InterestList): void {
    this.form.patchValue({
      name: `${demand.destination.name} weekly plan`, destinationId: demand.destination.id,
      startDate: demand.referenceDate, closingTime: demand.closingTime, departureTime: demand.departureTime,
      arrivalTime: demand.arrivalTime, returnDepartureTime: demand.returnDepartureTime,
      returnArrivalTime: demand.returnArrivalTime,
    });
    const day = new Intl.DateTimeFormat('en-US', { weekday: 'long', timeZone: 'UTC' })
      .format(new Date(`${demand.referenceDate}T00:00:00Z`)).toUpperCase() as DayOfWeek;
    this.selectedDays.set([day]);
  }

  private today(): string { return this.dateString(new Date()); }
  private weeksFromToday(weeks: number): string { const value = new Date(); value.setDate(value.getDate() + weeks * 7); return this.dateString(value); }
  private dateString(value: Date): string {
    const year = value.getFullYear();
    return `${year}-${String(value.getMonth() + 1).padStart(2, '0')}-${String(value.getDate()).padStart(2, '0')}`;
  }
}
