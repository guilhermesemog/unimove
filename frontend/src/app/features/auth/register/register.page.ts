import { Component, signal } from '@angular/core';
import { form, FormField, maxLength, minLength, required } from '@angular/forms/signals';

import { AuthService } from '../../../core/auth/auth.service';
import { TextFieldComponent } from '../../../shared/components/forms/text-field/text-field.component';

@Component({
  selector: 'app-register',
  imports: [FormField, TextFieldComponent],
  templateUrl: './register.html',
})
export class RegisterPage {

  constructor(private authService: AuthService) { }

  loginModel = signal({
    cpf: '',
    password: '',
    firstName: '',
    lastName: '',
    phone: '',
  });

  loginForm = form(this.loginModel, (schema) => {
    required(schema.cpf, { message: 'CPF is required' });
    minLength(schema.cpf, 11, { message: 'CPF must be 11 characters long' });
    maxLength(schema.cpf, 11, { message: 'CPF must be 11 characters long' });

    required(schema.password, { message: 'Password is required' });
    minLength(schema.password, 8, { message: 'Password must be at least 8 characters long' });

    required(schema.firstName, { message: 'First name is required' });
    required(schema.lastName, { message: 'Last name is required' });

    required(schema.phone, { message: 'Phone is required' });
    minLength(schema.phone, 8, { message: 'Phone must be at least 8 characters long' });
    maxLength(schema.phone, 12, { message: 'Phone must be at most 12 characters long' });
  });

  onSubmit() {
    if (!this.loginForm().valid()) {
      return;
    }

    this.authService.register(this.loginModel().cpf, this.loginModel().password, this.loginModel().firstName, this.loginModel().lastName, this.loginModel().phone);
  }
}
