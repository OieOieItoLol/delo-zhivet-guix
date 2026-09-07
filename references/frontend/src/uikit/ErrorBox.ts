import { css, l, type ClassComponent } from "../lib";

const errorBoxElementId = 'uikit-error-box'
const errorBoxElementTextId = 'uikit-error-box-text'

export class ErrorBox implements ClassComponent<HTMLDivElement> {
    mount(): HTMLDivElement {
        return l('div', _ => {
            _.id = errorBoxElementId
            _.style.display = 'none'
            const errorRoot = _

            css`
                font-size: 18px;
                padding: 16px;
                display: flex;
                justify-content: center;
                align-items: center;
                color: #bc0a2c;
                background-color: #ebebeb;
                border-radius: 10px;
            `.apply(_)

            l(_, 'span', _ => {
                _.id = errorBoxElementTextId
            })
            l(_, 'span', _ => {
                css`
                    cursor: pointer;
                    background-color: #bc0a2c;
                    display: inline-block;
                    width: 20px;
                    height: 20px;
                    mask: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-x"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>');
                    position: relative;
                    top: 1px;
                    left: 5px;
                `.apply(_)
                _.onclick = __ =>
                    errorRoot.style.display = 'none'
            })
        })
    }
}

export function setError(message: string) {
    const el = document.getElementById(errorBoxElementId) as HTMLDivElement | null
    if (el != null) {
        (document.getElementById(errorBoxElementId) as HTMLDivElement).innerText = message
        el.style.display = 'block'
    }
}