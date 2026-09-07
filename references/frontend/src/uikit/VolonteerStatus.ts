import { css, l, type ClassComponent } from "../lib";

export type VolonteerStatus = 'Active' | 'Banned' | 'InVacation'

export class VolonteerStatusView implements ClassComponent<HTMLDivElement> {
    constructor(
        readonly status: VolonteerStatus,
    ) { }

    mount(): HTMLDivElement {
        return l('div', _ => {
            css`
                height: 30px;
                width: 110px;
                display: flex;
                justify-content: center;
                align-items: center;
                border-radius: 30px;
            `.apply(_)

            const root = _

            l(_, 'span', _ => {
                css`
                    color: white;
                    font-weight: 600;
                    line-height: 20px;                    
                    font-size: 14px;
                `.apply(_)

                switch (this.status) {
                    case 'Active':
                        css`background-color: #34C759;`.apply(root)
                        _.innerText = 'Активный'
                        break;
                    case 'Banned':
                        css`background-color: rgba(0, 0, 0, 0.25);`.apply(root)
                        _.innerText = 'Забанен'
                        break;
                    case 'InVacation':
                        css`background-color: var(--base-orange-color);`.apply(root)
                        _.innerText = 'В отпуске'
                        break;
                }
            })
            if (this.status == 'InVacation')
                l(_, 'span', _ => {
                    css`
                        background-color: white;
                        color: black;
                        border-radius: 50%;
                        padding: 4px;
                        font-size: 10px;
                        font-weight: 600;
                        margin-left: 8px;
                        line-height: 12px;
                    `.apply(_)
                    _.innerText = '20'
                })
        })
    }
}