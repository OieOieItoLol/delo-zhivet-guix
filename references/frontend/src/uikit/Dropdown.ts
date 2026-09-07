import { css, l, lz, sync, type ClassComponent, type Component } from "../lib";

export class Dropdown<T extends Component<HTMLElement>> implements ClassComponent<HTMLDivElement> {
    constructor(
        readonly items: Array<T>
    ) { }

    mount(): HTMLDivElement {
        let isOpen = false

        return lz('div', (_, z) => {
            _.onmouseenter = () => {if (!isOpen) sync([z], [isOpen = !isOpen])}
            _.onmouseleave = () => {if (isOpen) sync([z], [isOpen = !isOpen])}
            l(_, 'div', _ => {
                css`
                    display: flex;
                    gap: 8px;
                    align-items: center;
                    background-color: #F872441A;
                    border-radius: 30px;
                    padding: 4px 8px;
                `.apply(_)

                if (this.items.length > 0) l(_, this.items[0])
                l(_, 'span', _ => {
                    _.innerText = '...'
                })

                l(_, 'span', _ => {
                    css`
                        content: '';
                        width: 16px;
                        height: 16px;
                        background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 16 16" fill="none"><path fill-rule="evenodd" clip-rule="evenodd" d="M1.69969 6.24214C1.39214 5.93459 1.39214 5.43595 1.69969 5.1284C2.00724 4.82085 2.50588 4.82085 2.81343 5.1284L7.99996 10.3149L13.1865 5.1284C13.494 4.82085 13.9927 4.82085 14.3002 5.1284C14.6078 5.43595 14.6078 5.93459 14.3002 6.24214L8.62334 11.919C8.27906 12.2633 7.72087 12.2633 7.37659 11.919L1.69969 6.24214Z" fill="black"/></svg>');
                    `.apply(_)

                    if (isOpen)
                        css`
                            transform: rotate(180deg);
                        `.apply(_)
                })
            })

            if (!isOpen || this.items.length == 0) return
        
            l(_, 'div', _ => {
                css`
                    position: absolute; 
                    background-color: white;                   
                    padding: 8px;
                    display: flex;
                    flex-direction: column;
                    border: 1px solid var(--base-border-color);
                    border-radius: 12px;
                `.apply(_)

                for (let i = 1; i < this.items.length; i++)
                    l(_, 'div', _ => {
                        css`padding: 4px;`.apply(_)
                        l(_, this.items[i])
                    })
            })
        })
    }
}