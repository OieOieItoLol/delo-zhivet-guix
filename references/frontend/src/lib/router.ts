import { l, lz, sync, type ClassComponent, type Component } from "../lib";

export class Link<A> {
    constructor(
        readonly prefix: string,
    ) { }

    href(_arguments: A): string {
        return this.prefix + (
            typeof _arguments === 'undefined'
                ? '' : encodeURIComponent(JSON.stringify(_arguments))
        )
    }
}

class Route {
    constructor(
        readonly prefix: string,
        readonly factory: (_arguments: any) => Component<HTMLElement>,
        readonly onActivate?: () => void,
        readonly onDeactivate?: () => void,
    ) { }
}

export class Routes {

    private routes: Array<Route> = []

    add<P>(
        prefix: string, componentFactory: (args: P) => Component<HTMLElement>,
        onActivate?: () => void, onDeactivate?: () => void
    ): Link<P> {

        this.routes.push(
            new Route(
                prefix, componentFactory,
                onActivate, onDeactivate
            )
        )

        return new Link(prefix)
    }
}

export class Router implements ClassComponent<HTMLDivElement> {

    constructor(readonly routes: Routes) { }

    mount(): HTMLDivElement {
        const routes = ((this.routes as any).routes as Array<Route>)
            .toSorted((r1, r2) => r2.prefix.length - r1.prefix.length)
        const currentRouteAndLocation = () => {

            const location = (window.location.hash || '#')
                .replace(/\/+$/, '')
            const route = routes.find(r => location.startsWith(r.prefix))
                || routes.find(r => r.prefix === '#')!

            return { location, route }
        }

        let prev: Route | null = null
        let current = currentRouteAndLocation()

        return lz('div', (_, z) => {

            const args = current.route.factory.length === 1 ? JSON.parse(
                decodeURIComponent(current.location.substring(current.route.prefix.length))) : undefined

            l(_, current.route.factory(args))

            if (prev != null && prev.onDeactivate != null)
                prev.onDeactivate()

            prev = current.route
            if (current.route.onActivate != null)
                current.route.onActivate()

            window.addEventListener('hashchange', () => {
                const c = currentRouteAndLocation()
                if (c.route !== prev)
                    sync([z], [current = c])
            })
        })
    }
}