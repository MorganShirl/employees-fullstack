import { Component, Inject } from '@angular/core';
import { MAT_SNACK_BAR_DATA, MatSnackBarRef } from '@angular/material/snack-bar';
import { MatButton } from '@angular/material/button';

@Component({
    standalone: true,
    selector: 'app-error-snackbar',
    styleUrl: './error-snackbar.component.scss',
    template: `
        <div class="error-snackbar">
            <div class="error-snackbar__lines">
                @for (line of data.lines; track $index) {
                    <div>{{ line }}</div>
                }
            </div>
            <button mat-button class="error-snackbar__close" (click)="close()">Close</button>
        </div>
    `,
    imports: [MatButton]
})
export class ErrorSnackbarComponent {
    constructor(
        @Inject(MAT_SNACK_BAR_DATA) public data: { lines: string[] },
        private readonly snackBarRef: MatSnackBarRef<ErrorSnackbarComponent>,
    ) {}

    close() {
        this.snackBarRef.dismiss();
    }
}
