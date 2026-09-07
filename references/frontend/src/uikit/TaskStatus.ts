import { css, l, type ClassComponent } from "../lib";

export type TaskStatus = 'New' | 'InProcessing' | 'InPlan' | 'Done' | 'Inactive' | 'Archive'

export class TaskStatusView implements ClassComponent<HTMLSpanElement> {
    constructor(
        readonly status: TaskStatus,
    ) { }

    mount(): HTMLSpanElement {
        return l('span', _ => {
            css`
                font-size: 14px;
                font-weight: 600;
                background-color: var(--base-orange-color);
                color: white;
                border-radius: 30px;
                padding: 4px 12px;
                display: inline-block;
                line-height: 18px;
            `.apply(_)

            switch (this.status) {
                case "New": _.innerText = 'Новая'; break
                case "InProcessing": _.innerText = 'В обработке'; break
                case "InPlan": _.innerText = 'В планах'; break
                case "Done": _.innerText = 'Выполнена'; break
                case "Inactive": _.innerText = 'Неактивна'; break
                case "Archive": _.innerText = 'Архив'; break
            }
        })
    }
}