import { apiCall, type AsyncClassComponent, css, l } from '../lib';
import type { Link } from '../lib/router';
import { stTable, stTableRowInactive } from '../uikit/Table';
import { type VolonteerStatus, VolonteerStatusView } from '../uikit/VolonteerStatus';

type Volonteer = {
    id: number,
    tgName: string,
    registerDate: string,
    status: VolonteerStatus,
    isBanned: boolean,
    activeTasksCount: number,
    finishedTasksCount: number,
}

export function formatStringDate(date: string) {
    let tmpDate: Date = new Date(date)
    let year = tmpDate.getFullYear()
    let month = (tmpDate.getMonth() + 1).toString().padStart(2, '0')
    let day = tmpDate.getDate().toString().padStart(2, '0');

    return `${year}-${month}-${day}`
}

export class VolonteersPage implements AsyncClassComponent<HTMLDivElement> {
    constructor(
        readonly volonteerEditLink: Link<Number>
    ) {}

    async mount(): Promise<HTMLDivElement> {

        const volonteersShort = await apiCall<Array<Volonteer>>('/manager/volunteer/volunteersShort', {})

        return l('div', _ => {
            css`
                padding: 16px;
                width: 100%;
            `.apply(_)

            l(_, 'table', _ => {
                stTable.apply(_)

                l(_, 'thead', _ => {
                    l(_, 'th', _ => { _.innerText = 'ID' })
                    l(_, 'th', _ => { _.innerText = 'Имя' })
                    l(_, 'th', _ => { _.innerText = 'Дата регистрации' })
                    l(_, 'th', _ => { _.innerText = 'Статус' })
                    l(_, 'th', _ => { _.innerText = 'Активных задач' })
                    l(_, 'th', _ => { _.innerText = 'Завершенных задач' })
                })
                l(_, 'tbody', _ => {
                    for (let v of volonteersShort)
                        l(_, 'tr', _ => {
                            if (v.status == 'Banned')
                                stTableRowInactive.apply(_)

                            l(_, 'td', _ => { _.innerText = '#' + v.id })
                            l(_, 'td', _ => {
                                l(_, 'a', _ => {
                                    _.innerText = v.tgName
                                    _.href = this.volonteerEditLink.href(v.id)
                                })
                            })
                            
                            l(_, 'td', _ => { _.innerText = formatStringDate(v.registerDate) })
                            l(_, 'td', _ => { l(_, new VolonteerStatusView(v.isBanned ? 'Banned' : v.status)) })
                            l(_, 'td', _ => { _.innerText = v.activeTasksCount + '' })
                            l(_, 'td', _ => { _.innerText = v.finishedTasksCount + '' })
                        })
                })
            })
        })
    }
}