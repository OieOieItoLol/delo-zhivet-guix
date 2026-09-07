import './app.css'
import { l, apiCall, type ClassComponent, css, type Component } from './lib.ts'
import { Link, Router, Routes } from './lib/router.ts'
import { KanbanBoardPage } from './pages/kanban/KanbanBoardPage.ts'
import { TaskEditPage } from './pages/TaskEditPage.ts'
import { VolonteersPage } from './pages/VolonteersPage.ts'
import './sidebar/sidebar.css'
import { VolonteerEditPage } from './pages/VolonteerEditPage.ts'
import { ArchivePage } from './pages/ArchivePage.ts'
import { LoginPage } from './pages/LoginPage.ts'
import { ActiveTagsPage } from './pages/tags/ActiveTagsPage.ts'
import { InactiveTagsPage } from './pages/tags/InactiveTagsPage.ts'
import { ErrorBox } from './uikit/ErrorBox.ts'


class MenuLink implements ClassComponent<HTMLLIElement> {
    constructor(
        readonly routes: Routes,
        readonly text: string,
        readonly path: string,
        readonly comp: Component<HTMLElement>,
        readonly active: boolean,
    ) { }

    mount(): HTMLLIElement {
        return l('li', el => {
            l(el, 'a', _ => {
                _.innerText = this.text
                _.href = this.routes.add(
                    this.path, () => this.comp
                ).href(undefined)

                if (this.active)
                    _.classList.add('active')
            })
        })
    }
}

class EmptyPage implements ClassComponent<HTMLDivElement> {
    mount(): HTMLDivElement {
        return l('div', _ => { _.innerText = 'empty page' })
    }
}

const ui = l('div', async _ => {

    const l1Routes = new Routes()
    const loginLink = l1Routes.add('#/login', () => new LoginPage())


    l1Routes.add<void>('#', () => l('div', _ => {
        _.className = 'app'

        const routes = new Routes()

        let volunteerEditLink!: Link<number>
        let taskEditLink!: Link<number>
        volunteerEditLink = routes.add('#/volonteerEdit/', (id: number) => new VolonteerEditPage(id, taskEditLink))
        taskEditLink = routes.add('#/taskEdit/', (id: number) => new TaskEditPage(id, volunteerEditLink))


        l(_, 'div', _ => {
            _.className = 'sidebar'
            const sidebar = _

            l(_, 'img', _ => { _.className = 'logo' })

            l(_, 'ul', _ => {
                _.onclick = ev => {
                    for (let el of sidebar.querySelectorAll('.active'))
                        el.classList.remove('active');

                    const el = (ev.target as HTMLElement)
                    el.classList.add('active')

                    if (!(el instanceof HTMLSpanElement)) return
                    const parent = el.parentElement!
                    parent.style.height = parent.style.height === '60px' ? '180px' : '60px'
                }

                function checkPath(...args: string[]): boolean {
                    return args.includes((window.location.hash || '#').replace(/\/+$/, ''))
                }

                _.className = 'menu'
                l(_, 'li', _ => {

                    _.style.height = '60px'
                    _.style.transition = 'height 0.4s ease'
                    _.style.overflow = 'hidden'

                    const ecologicalTasksPath = '#'
                    const socialTasksPath = '#/tasks/social'

                    if (checkPath(ecologicalTasksPath, socialTasksPath))
                        _.style.height = '180px'

                    l(_, 'span', _ => { _.innerText = 'Задачи' })
                    l(_, 'ul', _ => {
                        l(_, new MenuLink(routes, 'Экологические', ecologicalTasksPath,
                            new KanbanBoardPage(taskEditLink, 'Ecological'), checkPath(ecologicalTasksPath)
                        ))
                        l(_, new MenuLink(routes, 'Социальные', socialTasksPath,
                            new KanbanBoardPage(taskEditLink, 'Social'), checkPath(socialTasksPath)
                        ))
                    })
                })
                const volunteersPath = '#/volonteers'
                l(_, new MenuLink(routes, 'Волонтеры', volunteersPath, new VolonteersPage(volunteerEditLink), checkPath(volunteersPath)))

                l(_, 'li', _ => {

                    _.style.height = '60px'
                    _.style.transition = 'height 0.4s ease'
                    _.style.overflow = 'hidden'

                    const tagsActivePath = '#/tags/active'
                    const tagsInactivePath = '#/tags/inactive'

                    if (checkPath(tagsActivePath, tagsInactivePath))
                        _.style.height = '180px'

                    l(_, 'span', _ => { _.innerText = 'Тэги' })
                    l(_, 'ul', _ => {
                        l(_, new MenuLink(routes, 'Активные', tagsActivePath, new ActiveTagsPage(), checkPath(tagsActivePath)))
                        l(_, new MenuLink(routes, 'Неактивные', tagsInactivePath, new InactiveTagsPage(), checkPath(tagsInactivePath)))
                    })
                })
                const archivePath = '#/archive'
                l(_, new MenuLink(routes, 'Архив', archivePath, new ArchivePage(taskEditLink), checkPath(archivePath)))
                const settingsPath = '#/settings'
                l(_, new MenuLink(routes, 'Настройки', settingsPath, new EmptyPage(), checkPath(settingsPath)))
            })

            l(_, 'div', _ => {
                _.className = 'authentication'
                l(_, 'img', _ => { })
                l(_, 'div', async _ => {
                    type Auth = { username: string, role: string}
                    const user = await apiCall<Auth>('/manager/sidebar/currentUser', null)
                    

                    l(_, 'span', _ => {
                        css`
                            cursor: pointer;
                            &::after {
                                content: '';
                                display: inline-block;
                                width: 16px;
                                height: 16px;
                                background-image: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" class="feather feather-power"><path d="M18.36 6.64a9 9 0 1 1-12.73 0"/><line x1="12" y1="2" x2="12" y2="12"/></svg>');
                                position: relative;
                                top: 2px;
                                left: 2px;
                            }
                        `.apply(_)

                        _.title = 'Выйти'
                        _.innerText = user.username
                        _.onclick = async __ => {
                            await apiCall('/auth/logout', null)
                            window.location.href = loginLink.href(undefined)
                        }
                    })
                    l(_, 'span', _ => { _.innerText = (user.role == 'Admin' ? 'Админ' : 'Модератор') })
                })
            })
        })

        l(_, 'div', _ => {
            l(_, new ErrorBox())
            l(_, new Router(routes))
        })
    }))

    l(_, new Router(l1Routes))
})

ui.then(el => document.body.appendChild(el))