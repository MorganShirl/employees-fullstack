import { inject, Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ErrorSnackbarComponent } from '@shared/components/error-snackbar/error-snackbar.component';

@Injectable({ providedIn: 'root' })
export class NotificationService {
    private readonly snackBar = inject(MatSnackBar);

    showStickyError(message: string, action: string = 'Close') {
        this.snackBar.open(message, action, {
            panelClass: ['snackbar-error'],
            horizontalPosition: 'right',
            verticalPosition: 'bottom',
        });
    }

    showStickyErrorLines(lines: string[]) {
        this.snackBar.openFromComponent(ErrorSnackbarComponent, {
            data: { lines },
            panelClass: ['snackbar-error'],
            horizontalPosition: 'right',
            verticalPosition: 'bottom',
        });
    }
}
