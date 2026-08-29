import { Component, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';

import { AuthService } from '../../core/auth/auth.service';
import { UI_COPY } from '../../core/content/ui-copy';
import { BoardingStopService } from '../admin/boarding-stop/boarding-stop.service';
import { ConductorService } from '../admin/conductor/conductor.service';
import { StudentService } from '../admin/student/student.service';
import { HeaderComponent } from '../../shared/components/list-header/header.component';
import { UiStateComponent } from '../../shared/components/ui-state/ui-state.component';
import { CpfPipe } from '../../shared/pipes/cpf-pipe';
import { PhonePipe } from '../../shared/pipes/phone-pipe';
import { BoardingStop } from '../../shared/types/boarding-stop.type';
import { Conductor } from '../../shared/types/conductor.type';
import { Student } from '../../shared/types/student.type';
import { User, UserRole } from '../../shared/types/user.type';

@Component({
  selector: 'app-user-page',
  imports: [CpfPipe, HeaderComponent, PhonePipe, UiStateComponent],
  templateUrl: './user.page.html',
})
export class UserPage {
  private readonly authService = inject(AuthService);
  private readonly boardingStopService = inject(BoardingStopService);
  private readonly conductorService = inject(ConductorService);
  private readonly studentService = inject(StudentService);

  protected readonly copy = UI_COPY.student.profile;
  protected readonly loading = signal(true);
  protected readonly saving = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly feedback = signal<string | null>(null);
  protected readonly user = signal<User | null>(null);
  protected readonly student = signal<Student | null>(null);
  protected readonly conductor = signal<Conductor | null>(null);
  protected readonly boardingStops = signal<BoardingStop[]>([]);
  protected readonly selectedBoardingStopId = signal<number | null>(null);

  ngOnInit(): void {
    this.fetch();
  }

  protected fetch(): void {
    this.loading.set(true);
    this.error.set(null);
    this.authService.identify().subscribe({
      next: (user) => {
        this.user.set(user);
        if (user.role === UserRole.Student) this.fetchStudent(user.id);
        else if (user.role === UserRole.Conductor) this.fetchConductor(user.id);
        else this.loading.set(false);
      },
      error: () => {
        this.error.set('We could not load your profile. Please try again.');
        this.loading.set(false);
      },
    });
  }

  private fetchStudent(id: number): void {
    forkJoin({
      student: this.studentService.getStudentById(id),
      stops: this.boardingStopService.getBoardingStopsAsList(),
    }).subscribe({
      next: ({ student, stops }) => {
        this.student.set(student);
        this.boardingStops.set(stops);
        this.selectedBoardingStopId.set(student.preferredBoardingStop?.id ?? null);
      },
      error: () => this.error.set('We could not load your student information.'),
      complete: () => this.loading.set(false),
    });
  }

  private fetchConductor(id: number): void {
    this.conductorService.getConductorById(id).subscribe({
      next: (conductor) => this.conductor.set(conductor),
      error: () => this.error.set('We could not load your driver information.'),
      complete: () => this.loading.set(false),
    });
  }

  protected selectBoardingStop(event: Event): void {
    this.selectedBoardingStopId.set(Number((event.target as HTMLSelectElement).value));
    this.feedback.set(null);
  }

  protected saveBoardingStop(): void {
    const id = this.selectedBoardingStopId();
    if (id === null || this.saving()) return;

    this.saving.set(true);
    this.error.set(null);
    this.studentService.updatePreferredBoardingStop(id).subscribe({
      next: (student) => {
        this.student.set(student);
        this.feedback.set(this.copy.saved);
      },
      error: () => {
        this.error.set('We could not update your boarding stop. Please try again.');
        this.saving.set(false);
      },
      complete: () => this.saving.set(false),
    });
  }
}
