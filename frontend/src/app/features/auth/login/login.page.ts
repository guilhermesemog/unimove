import { Component, signal } from '@angular/core';
import { form, FormField, required } from '@angular/forms/signals';

import { AuthService } from '../../../core/auth/auth.service';
import { TextFieldComponent } from '../../../shared/components/forms/text-field/text-field.component';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-login',
  imports: [FormField, TextFieldComponent, MatIconModule],
  templateUrl: './login.html',
})
export class LoginPage {

  constructor(private authService: AuthService) { }

  loginModel = signal({
    cpf: '',
    password: '',
  });

  loginForm = form(this.loginModel, (schema) => {
    required(schema.cpf, { message: 'CPF é obrigatório' });
    required(schema.password, { message: 'Senha é obrigatória' });
  });

  async onSubmit() {
    if (!this.loginForm().valid()) {
      return;
    }

    await this.authService.login(this.loginModel().cpf, this.loginModel().password);
  }
}