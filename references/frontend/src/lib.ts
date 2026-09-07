// import { setError } from "./uikit/ErrorBox"

import { setError } from "./uikit/ErrorBox"

export function css(str: any, ...args: any): Css {
    args[0] // unused
    str // unused
    return new Css(0)
}

export class Css {
    constructor(
        public readonly id: number
    ) { }

    apply(dest: HTMLElement): void {
        dest.classList.add('g' + this.id)
    }
    unapply(dest: HTMLElement): void {
        dest.classList.remove('g' + this.id)
    }
    toggle(dest: HTMLElement): void {
        dest.classList.toggle('g' + this.id)
    }
}


export interface ClassComponent<T extends HTMLElement> {
    mount(): T
}

export interface AsyncClassComponent<T extends HTMLElement> {
    mount(): Promise<T>
}

export type FnComponent<T extends HTMLElement> = () => T
export type AsyncFnComponent<T extends HTMLElement> = () => Promise<T>

export type Component<T extends HTMLElement> =
    T | FnComponent<T> | AsyncFnComponent<T> |
    ClassComponent<T> | AsyncClassComponent<T>

export function l<T extends keyof HTMLElementTagNameMap, V extends void | Promise<void>>(
    comp: T, init: (_: HTMLElementTagNameMap[T]) => V
): V extends void ? HTMLElementTagNameMap[T] : Promise<HTMLElementTagNameMap[T]>

export function l<T extends keyof HTMLElementTagNameMap>(
    dest: HTMLElement, comp: T, init: (_: HTMLElementTagNameMap[T]) => void | Promise<void>
): void

export function l<T extends Component<HTMLElement>>(
    dest: HTMLElement, comp: T
): void

export function l(): any {
    if (typeof arguments[0] === 'string') {
        const element = document.createElement(arguments[0] as string)
        const init = arguments[1] as (_: HTMLElement) => void | Promise<void>
        const res = init(element)

        if (res instanceof Promise)
            return res.then(_ => element)
        else
            return element

    } else {

        const dest = arguments[0] as HTMLElement

        if (typeof arguments[1] === 'string') {
            const element = document.createElement(arguments[1] as string)
            const init = arguments[2] as (_: HTMLElement) => void | Promise<void>
            const res = init(element)

            if (res instanceof Promise) {
                const anchor = document.createElement('div')
                dest.appendChild(anchor)
                res.then(_ => dest.replaceChild(element, anchor))
            } else
                dest.appendChild(element)

        } else {
            const mount = (res: HTMLElement | Promise<HTMLElement>) => {
                if (res instanceof Promise)
                    res.then(v => dest.appendChild(v))
                else
                    dest.appendChild(res)
            }

            const comp = arguments[1]

            if (comp instanceof HTMLElement)
                mount(comp)
            else if (comp instanceof Function)
                mount(comp() as HTMLElement | Promise<HTMLElement>)
            else
                mount(comp.mount() as HTMLElement | Promise<HTMLElement>)
        }
    }
}

export class Zone {
    constructor(
        public root: HTMLElement,
        public factory: () => HTMLElement,
        public init: (_: HTMLElement, z: Zone) => void | Promise<void>
    ) { }

    remount(): void {

        const newEl = this.factory()
        const res = this.init(newEl, this)

        if (res instanceof Promise) {
            res.then(_ => {
                if (this.root.parentNode === null) return

                this.root.parentNode.replaceChild(newEl, this.root)
                this.root = newEl
            })
        } else {
            // FIXME: deduplicate

            if (this.root.parentNode === null) return

            this.root.parentNode.replaceChild(newEl, this.root)
            this.root = newEl
        }
    }
}

export function lz<T extends keyof HTMLElementTagNameMap, V extends void | Promise<void>>(
    comp: T, init: (_: HTMLElementTagNameMap[T], z: Zone) => V
): V extends void ? HTMLElementTagNameMap[T] : Promise<HTMLElementTagNameMap[T]>

export function lz<T extends keyof HTMLElementTagNameMap>(
    dest: HTMLElement, comp: T, init: (_: HTMLElementTagNameMap[T], z: Zone) => void | Promise<void>
): Zone

export function lz(): any {
    if (typeof arguments[0] === 'string') {
        const factory = () => document.createElement(arguments[0])
        const root = factory()
        const init = arguments[1] as (_: HTMLElement, z: Zone) => void | Promise<void>
        const z = new Zone(root, factory, init)
        const res = init(root, z)

        if (res instanceof Promise)
            return res.then(_ => root)
        else
            return root

    } else {
        const dest = arguments[0] as HTMLElement
        const factory = () => document.createElement(arguments[1])
        const root = factory()
        const init = arguments[2] as (_: HTMLElement, z: Zone) => void | Promise<void>
        const z = new Zone(root, factory, init)
        const res = init(root, z)

        if (res instanceof Promise) {
            const anchor = document.createElement('div')
            dest.appendChild(anchor)
            res.then(_ => dest.replaceChild(root, anchor))
        } else
            dest.appendChild(root)

        return z;
    }
}

export function sync(
    zones: Array<Zone | null>, changes: any
) {
    changes
    for (let z of zones)
        if (z !== null) z.remount()
}


type Reponse<T> =
    {
        isOk: true,
        value: T
    } | {
        isOk: false,
        error: {
            kind: string,
            message: string,
        }
    }


export async function apiCall<T>(path: string, args: any): Promise<T> {
    const resp = await window.fetch('/api' + path,
        args instanceof FormData ? {
            method: "POST",
            body: args,
        } : {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(args),
        }
    )

    if (resp.status == 401) {
        window.location.hash = '#/login'
        return null as unknown as Promise<T> // FIXME: return 'unauth error'
    }

    if (resp.status != 200) setError(
        'Ошибка соединения с сервером. Не все данные сохранены. Обновите страницу'
    )

    const body = await resp.json() as Reponse<T>

    switch (body.isOk) {
        case true:
            return body.value
        case false:
            setError(
                'Ошибка обработки запроса: ' + body.error.message +
                '. Не все данные сохранены. Обновите страницу'
            )
            throw new Error(body.error.message)
    }
}

export function debounce<T extends (...args: Parameters<T>) => ReturnType<T>>(
    callback: T, delay: number = 500
): (...args: Parameters<T>) => void {

    let timer: number | null = null

    return function (this: ThisParameterType<T>, ...args: Parameters<T>): void {
        if (timer != null) clearTimeout(timer)
        timer = setTimeout(() => { callback.apply(this, args) }, delay)
    }
}