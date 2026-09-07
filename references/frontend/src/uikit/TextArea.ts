import { css, l, type ClassComponent } from "../lib";

export class TextArea implements ClassComponent<HTMLDivElement> {
    constructor(
        readonly value: string,
        readonly placeholder: string,
        readonly elementInit?: (el: HTMLTextAreaElement) => void,
    ) { }

    mount(): HTMLDivElement {
        return l('div', _ => {
            css`
                border: 1px solid var(--base-border-color);
                border-radius: 20px;
                padding: 8px;
                display: flex;
                width: min(100%, 320px);
            `.apply(_)
            l(_, 'textarea', _ => {
                css`
                    height: 144px;
                    width: 100%;
                    border: none;
                    outline: none;
                    margin: 0;
                `.apply(_)

                _.value = this.value
                _.placeholder = this.placeholder

                if (this.elementInit)
                    this.elementInit(_)
            })
        })
    }
}