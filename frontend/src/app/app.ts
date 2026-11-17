import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { SessionService } from './core/session';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
})
export class App {
  protected readonly session = inject(SessionService);
  private readonly router = inject(Router);

  protected deconnexion(): void {
    this.session.fermer();
    void this.router.navigate(['/connexion']);
  }
}
