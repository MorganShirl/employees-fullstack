import { Injectable } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { RouterStateSnapshot, TitleStrategy } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { take } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class I18nTitleStrategy extends TitleStrategy {
    private lastSnapshot: RouterStateSnapshot | null = null;

    constructor(private readonly title: Title,
                private readonly translateService: TranslateService) {
        super();

        // When language changes, recompute the current page title
        this.translateService.onLangChange.subscribe(() => {
            if (this.lastSnapshot) {
                this.applyTitle(this.lastSnapshot);
            }
        });
    }

    override updateTitle(snapshot: RouterStateSnapshot): void {
        this.lastSnapshot = snapshot;
        this.applyTitle(snapshot);
    }

    private applyTitle(snapshot: RouterStateSnapshot): void {
        const titleKey = this.buildTitle(snapshot);
        if (titleKey) {
            // Wait for translations (handles first-time lang load too)
            this.translateService.stream(titleKey).pipe(take(1)).subscribe(translatedTitle => {
                this.title.setTitle(translatedTitle);
            });
        }
    }
}