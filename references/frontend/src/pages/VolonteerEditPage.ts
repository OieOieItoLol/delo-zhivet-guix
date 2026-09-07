import L from "leaflet";
import { apiCall, css, l, lz, sync, debounce, Zone, type AsyncClassComponent } from "../lib";
import { Button } from "../uikit/Button";
import { stFormLayout } from "../uikit/Form";
import { ConfirmModal } from "../uikit/Modal";
import { stBackButton, stFormAndMapLayout, stPageHeading } from '../uikit/Page';
import { TextArea } from "../uikit/TextArea";
import { VolonteerStatusView, type VolonteerStatus } from "../uikit/VolonteerStatus";
import { formatStringDate } from "./VolonteersPage";
import { Dropdown } from "../uikit/Dropdown";
import type { Link } from "../lib/router";
import { Select } from "../uikit/Select";

type Task = {
    id: number,
    name: string,
    longitude: number,
    latitude: number
}

type Volonteer = {
    id: number,
    tgName: string,
    status: 'Active' | 'InVacation',
    isBanned: boolean,
    registerDate: string,
    note: string,
    isManager: boolean,
    idManager: number | null,
    longitude: number,
    latitude: number,
    radius: number,
    tasks: Array<Task>
}

export class VolonteerEditPage implements AsyncClassComponent<HTMLDivElement> {
    constructor(
        readonly volonteerId: number,
        readonly taskEditLink: Link<number>
    ) { }

    async mount(): Promise<HTMLDivElement> {
        const v: Volonteer = await apiCall<Volonteer>('/manager/volunteer/get', this.volonteerId)
        let status: VolonteerStatus = v.isBanned ? 'Banned' : v.status
        let zStatus: Zone | null = null

        return l('div', _ => {
            l(_, 'div', _ => {
                css`
                    display: flex;
                    align-items: baseline;
                `.apply(_)

                l(_, 'button', _ => {
                    stBackButton.apply(_)
                    _.onclick = () => { history.back() }
                })
                l(_, 'span', _ => {
                    stPageHeading.apply(_)
                    _.innerText = 'Профиль волонтера ' + v.tgName
                })
            })
            l(_, 'div', _ => {
                css`
                    padding: 24px;
                    display: flex;
                    flex-direction: column;
                    gap: 32px;
                `.apply(_)

                l(_, 'div', _ => {
                    stFormAndMapLayout.apply(_)

                    l(_, 'div', _ => {
                        stFormLayout.apply(_)

                        l(_, 'div', _ => {
                            l(_, 'span', _ => {
                                css`
                                    &::before {
                                        background: url("/public/icons/task-id.svg") no-repeat 0px 0px transparent;
                                    }
                                `.apply(_)
                                _.innerText = 'ID:'
                            })
                            l(_, 'span', _ => { _.innerText = '#' + v.id })
                        })
                        l(_, 'div', _ => {
                            l(_, 'span', _ => {
                                css`
                                    &::before {
                                        height: 24px !important;
                                        top: 5px !important;
                                        background: url("/public/icons/task-status.svg") no-repeat 0px 0px transparent;
                                    }
                                `.apply(_)
                                _.innerText = 'Статус:'
                            })
                            zStatus = lz(_, 'div', _ => {
                                l(_, new VolonteerStatusView(v.isBanned ? 'Banned' : v.status))
                            })
                        })
                        l(_, 'div', _ => {
                            l(_, 'span', _ => {
                                css`
                                    &::before {
                                        background: url("/public/icons/task-date.svg") no-repeat 0px 0px transparent;
                                    }
                                `.apply(_)
                                _.innerText = 'Дата:'
                            })
                            l(_, 'input', _ => {
                                // FIXME: common styles for date input ???

                                css`
                                    font-size: 16px;
                                    width: 115px;
                                    border: none;
                                        outline: none;
                                `.apply(_)
                                _.type = 'date'
                                _.value = formatStringDate(v.registerDate)
                                _.disabled = true
                            })
                        })

                        lz(_, 'div', async (_, zRole) => {
                            l(_, 'span', _ => {
                                css`
                                    &::before {
                                        height: 22px !important;
                                        top: 4px !important;
                                        background: url("/public/icons/volonteer-role.svg") no-repeat 0px 0px transparent;
                                    }
                                `.apply(_)
                                _.innerText = 'Роль:'
                            })

                            const isAdmin = await apiCall<boolean>('/auth/isAdmin', {})

                            if (!isAdmin) {
                                l(_, 'span', _ => {
                                    _.innerText = (v.isManager ? 'Админ' : 'Волонтер')
                                })
                            } else {
                                l(_, new Select<'Волонтер' | 'Админ'>(
                                    v.isManager ? 'Админ' : 'Волонтер',
                                    [v.isManager ? 'Админ' : 'Волонтер', v.isManager ? 'Волонтер' : 'Админ'],
                                    role => l('span', _ => {
                                        _.innerText = role
                                    }),
                                    async role => {
                                        if (role == 'Админ' && !v.isManager) {
                                            sync([zRole], [v.idManager = await apiCall('/admin/add', v.tgName), v.isManager = true])
                                        }
                                        if (role == 'Волонтер' && v.isManager) {
                                            sync([zRole], [await apiCall('/admin/delete', v.idManager), v.isManager = false, v.idManager = null])
                                        }
                                    }
                                ))
                            }

                        })
                        l(_, 'div', _ => {
                            l(_, 'span', _ => {
                                css`
                                    &::before {
                                        height: 24px !important;
                                        top: 5px !important;
                                        background: url("/public/icons/task-volonteers.svg") no-repeat 0px 0px transparent;
                                    }
                                `.apply(_)
                                _.innerText = 'Задачи:'
                            })
                            l(_, 'span', _ => {
                                l(_, new Dropdown(v.tasks.map(t =>
                                    l('a', _ => {
                                        _.href = this.taskEditLink.href(t.id)
                                        _.innerText = t.name.substring(0, 20) + '...'
                                    })
                                )))
                            })

                        })
                        l(_, 'div', _ => {
                            l(_, 'span', _ => {
                                css`
                                    &::before {
                                        background: url("/public/icons/task-notes.svg") no-repeat 0px 0px transparent;
                                    }
                                `.apply(_)
                                _.innerText = 'Заметки:'
                            })

                            l(_, new TextArea(v.note, 'Введите текст заметки', _ => {
                                _.onkeyup = debounce((ev: Event) =>
                                    apiCall('/manager/volunteer/editNote',
                                        { 'id': v.id, 'note': (ev.target as HTMLTextAreaElement).value })
                                )
                            }))
                        })
                    })

                    l(_, 'div', _ => {
                        const map = L.map(_).setView({ lng: v.longitude, lat: v.latitude }, 14)

                        L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
                            maxZoom: 19,
                            attribution: '&copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap contributors</a>'
                        }).addTo(map);


                        L.control.scale({ imperial: true, metric: true }).addTo(map)

                        L.marker({ lng: v.longitude, lat: v.latitude })
                            .bindTooltip(v.tgName, { permanent: true }).openTooltip()
                            .addTo(map)

                        L.circle({ lng: v.longitude, lat: v.latitude }, { radius: v.radius })
                            .addTo(map)

                        for (let t of v.tasks) {
                            L.marker(
                                { lng: t.longitude, lat: t.latitude })

                                .bindTooltip(t.name.substring(0, 20) + '...', { permanent: true }).openTooltip()
                                .addTo(map)
                        }

                        // WTF?
                        setTimeout(() => { map.invalidateSize() }, 100)
                    })
                })

                lz(_, 'div', (_, z) => {
                    css`
                        align-self: center;
                        flex-grow: 0;
                    `.apply(_)

                    const isBanAction = status != 'Banned';
                    const actionText = isBanAction ? 'Забанить' : 'Разбанить'

                    l(_, new ConfirmModal(
                        actionText + v.tgName + '?',
                        new Button(actionText, isBanAction ? 'Normal' : 'Outline'),
                        async () => {
                            sync([zStatus, z], [
                                await apiCall('/manager/volunteer/ban', { volunteerId: v.id, isBanned: isBanAction }),
                                v.isBanned = isBanAction,
                                status = v.isBanned ? 'Banned' : v.status])
                        }
                    ))
                })
            })
        })
    }
}