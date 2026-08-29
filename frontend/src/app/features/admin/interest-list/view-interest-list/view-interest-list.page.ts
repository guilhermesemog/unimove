import { Component, computed, inject, Input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { ConfirmDialogComponent } from '../../../../shared/components/confirm-dialog/confirm-dialog.component';
import { DataTableComponent } from '../../../../shared/components/data-table/data-table.component';
import { TableColumn } from '../../../../shared/components/data-table/table-column.type';
import { HeaderComponent } from '../../../../shared/components/list-header/header.component';
import { OccupancyMeterComponent } from '../../../../shared/components/occupancy-meter/occupancy-meter.component';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';
import { RouteTimelineComponent } from '../../../../shared/components/route-timeline/route-timeline.component';
import { StatusChipComponent } from '../../../../shared/components/status-chip/status-chip.component';
import { UiStateComponent } from '../../../../shared/components/ui-state/ui-state.component';
import { DateLabelPipe } from '../../../../shared/pipes/date-label.pipe';
import { TimeLabelPipe } from '../../../../shared/pipes/time-label.pipe';
import { AdminOperation } from '../../../../shared/types/admin-operation.type';
import { Booking } from '../../../../shared/types/booking.type';
import { Conductor } from '../../../../shared/types/conductor.type';
import { Vehicle } from '../../../../shared/types/vehicle.type';
import { operationAttention, operationStage } from '../../../../shared/utils/admin-operation';
import { ConfirmDialogController } from '../../../../shared/utils/confirm-dialog.controller';
import { ListState } from '../../../../shared/utils/list-state';
import { BookingService } from '../../booking/booking.service';
import { ConductorService } from '../../conductor/conductor.service';
import { AdminOperationService } from '../../operations/admin-operation.service';
import { TripService } from '../../trip/trip.service';
import { VehicleService } from '../../vehicle/vehicle.service';

@Component({
  selector: 'app-view-interest-list-page',
  imports: [ConfirmDialogComponent, DataTableComponent, DateLabelPipe, FormsModule, HeaderComponent, OccupancyMeterComponent, PaginationComponent, RouteTimelineComponent, StatusChipComponent, TimeLabelPipe, UiStateComponent],
  templateUrl: './view-interest-list.page.html',
})
export class ViewInterestListPage {
  @Input() id!: number;

  private readonly operationService = inject(AdminOperationService);
  private readonly bookingService = inject(BookingService);
  private readonly tripService = inject(TripService);
  private readonly conductorService = inject(ConductorService);
  private readonly vehicleService = inject(VehicleService);

  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly feedback = signal<string | null>(null);
  protected readonly operation = signal<AdminOperation | null>(null);
  protected readonly conductors = signal<Conductor[]>([]);
  protected readonly vehicles = signal<Vehicle[]>([]);
  protected readonly selectedConductorId = signal<number | null>(null);
  protected readonly selectedVehicleId = signal<number | null>(null);
  protected readonly activeTab = signal<'overview' | 'passengers' | 'assignment'>('overview');
  protected readonly confirmDialog = new ConfirmDialogController();

  protected readonly attention = computed(() => this.operation() ? operationAttention(this.operation()!) : []);
  protected readonly stage = computed(() => this.operation() ? operationStage(this.operation()!) : 'Planning');
  protected readonly capacityExceeded = computed(() => {
    const operation = this.operation();
    const vehicle = this.vehicles().find((item) => item.id === this.selectedVehicleId());
    return !!operation && !!vehicle && operation.bookingCount > vehicle.capacity;
  });
  protected readonly assignableConductors = computed(() => {
    const date = this.operation()?.demand.referenceDate;
    const assignedConductorId = this.operation()?.trip?.conductor?.user.id;

    return this.conductors()
      .filter((driver) => driver.user.id === assignedConductorId
        || driver.user.active && (!date || driver.licenseExpirationDate >= date))
      .sort((a, b) => a.user.firstName.localeCompare(b.user.firstName));
  });
  protected readonly orderedVehicles = computed(() => {
    const bookings = this.operation()?.bookingCount ?? 0;
    return [...this.vehicles()].sort((a, b) => {
      const aFits = a.capacity >= bookings ? 0 : 1;
      const bFits = b.capacity >= bookings ? 0 : 1;
      return aFits - bFits || a.capacity - b.capacity;
    });
  });

  protected readonly passengerList = new ListState<Booking>({
    initialSortBy: 'id',
    initialSortDirection: 'asc',
    initialPageSize: 10,
    fetchAll: (query) => this.bookingService.getBookingsByInterestListId(this.id, query),
    fetchByQuery: (_term, query) => this.bookingService.getBookingsByInterestListId(this.id, query),
  });
  protected readonly passengerColumns: TableColumn<Booking>[] = [
    { key: 'student', label: 'Student', format: (booking) => `${booking.student.user.firstName} ${booking.student.user.lastName}` },
    { key: 'tripType', label: 'Trip', format: (booking) => booking.tripType.replace('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase()) },
    { key: 'boardingLocation', label: 'Boarding Stop', format: (booking) => booking.boardingLocation.local },
    { key: 'bookingStatus', label: 'Status' },
  ];

  ngOnInit(): void {
    this.fetch();
    this.passengerList.fetch();
  }

  protected fetch(): void {
    this.loading.set(true);
    this.error.set(null);
    forkJoin({
      operations: this.operationService.getOperations(),
      conductors: this.conductorService.getConductorsAsList(),
      vehicles: this.vehicleService.getVehiclesAsList(),
    }).subscribe({
      next: ({ operations, conductors, vehicles }) => {
        const operation = operations.find((item) => item.demand.id === Number(this.id)) ?? null;
        this.operation.set(operation);
        this.conductors.set(conductors);
        this.vehicles.set(vehicles);
        this.selectedConductorId.set(operation?.trip?.conductor?.user.id ?? null);
        this.selectedVehicleId.set(operation?.trip?.vehicle?.id ?? null);
        if (!operation) this.error.set('This operation could not be found.');
      },
      error: () => { this.error.set('We could not load this operation. Please try again.'); this.loading.set(false); },
      complete: () => this.loading.set(false),
    });
  }

  protected generateTrip(): void {
    const operation = this.operation();
    if (!operation || operation.trip) return;
    this.confirmDialog.open({
      title: 'Generate Trip',
      message: `Create a trip from ${operation.bookingCount} bookings for ${operation.demand.destination.name}?`,
      confirmLabel: 'Generate Trip',
      variant: 'default',
      action: () => this.tripService.createTrip({ interestListId: operation.demand.id }).subscribe(() => {
        this.feedback.set('Trip generated. Assign a driver and vehicle to complete planning.');
        this.activeTab.set('assignment');
        this.fetch();
      }),
    });
  }

  protected selectConductor(value: number | null): void {
    this.selectedConductorId.set(value);
  }

  protected selectVehicle(value: number | null): void {
    this.selectedVehicleId.set(value);
  }

  protected conductorAvailabilityLabel(driver: Conductor): string {
    const operationDate = this.operation()?.demand.referenceDate;
    if (!driver.user.active) return ' · Inactive';
    if (operationDate && driver.licenseExpirationDate < operationDate) return ' · License invalid for this date';
    return '';
  }

  protected saveAssignment(): void {
    const trip = this.operation()?.trip;
    const conductorId = this.selectedConductorId();
    const vehicleId = this.selectedVehicleId();
    if (!trip || conductorId === null || vehicleId === null || this.saving()) return;
    if (conductorId === trip.conductor?.user.id && vehicleId === trip.vehicle?.id) return;

    this.saving.set(true);
    this.tripService.updateTripAssignment(trip.id, conductorId, vehicleId).subscribe({
      next: () => { this.feedback.set('Assignment updated.'); this.fetch(); },
      error: () => { this.error.set('We could not update this assignment.'); this.saving.set(false); },
      complete: () => this.saving.set(false),
    });
  }
}
