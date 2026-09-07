import { css, l, type ClassComponent } from "../lib";


// TODO: вынести общие стили кнопки в no-export baseButton

export const stButton = css`
    min-width: 128px;
    height: 40px;
    border-radius: 30px;
    color: white;
    font-size: 16px;
    font-weight: 600;
    border: none;
    background-color: var(--base-orange-color);
    padding: 0px 24px;
    cursor: pointer;
`
export const stButtonOutline = css`
    min-width: 128px;
    height: 40px;
    border-radius: 30px;
    color: black;
    font-size: 16px;
    font-weight: 600;
    background-color: white;
    border: 1px solid var(--base-orange-color);
    padding: 0px 24px;
    cursor: pointer;
`
type ButtonMode = 'Normal' | 'Outline'
export class Button implements ClassComponent<HTMLButtonElement> {
    constructor(
        readonly caption: string,
        readonly mode?: ButtonMode,
        readonly elInit?: (el: HTMLButtonElement) => void
    ) { }

    mount(): HTMLButtonElement {
        return l('button', _ => {

            if (this.elInit != null)
                this.elInit(_);

            _.innerText = this.caption
            if (this.mode == 'Normal')
                stButton.apply(_)
            else
                stButtonOutline.apply(_)
        })
    }
}