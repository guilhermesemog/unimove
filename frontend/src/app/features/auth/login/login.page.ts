import { Component, signal } from '@angular/core';
import { form, FormField, required } from '@angular/forms/signals';

import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-login',
  imports: [FormField],
  templateUrl: './login.html',
})
export class LoginPage {

  constructor(private authService: AuthService) { }

  loginModel = signal({
    cpf: '',
    password: '',
  });

  loginForm = form(this.loginModel, (schema) => {
    required(schema.cpf);
    required(schema.password);
  });

  async onSubmit() {

    if (!this.loginForm().valid()) {
      return;
    }

    await this.authService.login(this.loginModel().cpf, this.loginModel().password);
  }

}
