import { Component, inject, signal } from '@angular/core';
import { UserService } from '../admin/user/user.service';
import { AuthService } from '../../core/auth/auth.service';
import { User, UserRole } from '../../shared/types/user.type';
import { HeaderComponent } from '../../shared/components/list-header/header.component';
import { CpfPipe } from '../../shared/pipes/cpf-pipe';
import { PhonePipe } from '../../shared/pipes/phone-pipe';
import { EnumPipe } from '../../shared/pipes/enum-pipe';
import { CapitalizePipe } from '../../shared/pipes/capitalize-pipe';
import { Student } from '../../shared/types/student.type';
import { Conductor } from '../../shared/types/conductor.type';
import { ConductorService } from '../admin/conductor/conductor.service';
import { StudentService } from '../admin/student/student.service';

@Component({
  selector: 'app-user.page',
  imports: [HeaderComponent, CpfPipe, PhonePipe, CapitalizePipe],
  templateUrl: './user.page.html',
})
export class UserPage {
  private authService = inject(AuthService);
  private userService = inject(UserService);
  private studentService = inject(StudentService);
  private conductorService = inject(ConductorService);

  user = signal<User | null>(null);

  student = signal<Student | null>(null);
  conductor = signal<Conductor | null>(null);

  ngOnInit() {
    this.fetchUser();
  }

  fetchUser() {
    this.authService.identify()
      .subscribe((user) => {
        this.user.set(user);

        if (this.user()!.role === UserRole.Student) {
          this.fetchStudent();
        }

        if (this.user()!.role === UserRole.Conductor) {
          this.fetchConductor();
        }
      });
  }

  fetchStudent() {
    this.studentService.getStudentById(this.user()!.id)
      .subscribe((student) => {
        this.student.set(student);
      });
  }

  fetchConductor() {
    this.conductorService.getConductorById(this.user()!.id)
      .subscribe((conductor) => {
        this.conductor.set(conductor);
      });
  }
}
