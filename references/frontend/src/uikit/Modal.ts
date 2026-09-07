import { css, l, lz, sync, type ClassComponent, type Component } from "../lib";
import { stButton, stButtonOutline } from "./Button";

export class Modal implements ClassComponent<HTMLDivElement> {
    constructor(
        readonly viewFactory: (onFinish: () => void) => Component<HTMLElement>,
        readonly opener: Component<HTMLElement>
    ) { }

    mount(): HTMLDivElement {
        let isOpened = false

        return lz('div', _ => {
            const zIsOpened = lz(_, 'div', _ => {
                if (!isOpened) return

                css`
                    position: fixed;
                    width: 100%;
                    height: 100%;
                    top: 0;
                    left: 0;
                    background-color: rgba(0, 0, 0, 0.9);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    /* leaflet devs - fuck you, bitches */
                    z-index: 9000; 
                `.apply(_)

                l(_, this.viewFactory(() => sync([zIsOpened], [isOpened = false])))
            })

            l(_, 'div', _ => {
                l(_, this.opener)
                _.onclick = () => sync([zIsOpened], [isOpened = true])
            })
        })
    }
}

export class ConfirmModal implements ClassComponent<HTMLDivElement> {
    constructor(
        readonly proposition: string,
        readonly opener: Component<HTMLElement>,
        readonly onConfirm: () => void,
    ) { }

    mount(): HTMLDivElement {

        return l('div', _ => {
            l(_, new Modal(
                onModalFinish => l('div', _ => {
                    css`
                        width: 365px;
                        height: 140px;
                        background-color: white;
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                        border-radius: 30px;
                        padding: 24px 32px;
                    `.apply(_)

                    l(_, 'span', _ => {
                        css`
                            margin-bottom: 24px;
                            font-size: 18px;
                            font-weight: 600;
                        `.apply(_)
                        _.innerText = this.proposition
                    })

                    const doJob = () => {
                        this.onConfirm()
                        onModalFinish()
                    }

                    l(_, 'div', _ => {
                        css`
                            display: flex;
                            gap: 10px;
                        `.apply(_)

                        l(_, 'button', _ => {
                            stButtonOutline.apply(_)
                            _.innerText = 'Нет'
                            _.onclick = __ => onModalFinish()
                        })
                        l(_, 'button', _ => {
                            stButton.apply(_)
                            _.innerText = 'Да'
                            _.onclick = __ => doJob()
                        })
                    })
                }),
                this.opener))
        })
    }
}