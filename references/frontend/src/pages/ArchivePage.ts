import { apiCall, css, l, lz, sync, Zone, type AsyncClassComponent } from "../lib";
import type { Link } from "../lib/router";
import { stTable } from "../uikit/Table";
import { TaskStatusView, type TaskStatus } from "../uikit/TaskStatus";

type ArchiveTask = {
    id: number,
    name: string,
    archiveDate: string,
    status: TaskStatus,
    volonteerCount: number,
    leaderTgName: string,
}

export class ArchivePage implements AsyncClassComponent<HTMLDivElement> {
    constructor(
        readonly taskEditLink: Link<number>,
    ) { }

    async mount(): Promise<HTMLDivElement> {

        const years = await apiCall<Array<number>>('/manager/task/getArchiveTaskYears', {})
        const fetchByYear = async (year: number) => {
            return await apiCall<Array<ArchiveTask>>('/manager/task/getArchiveTasksShort', year);
        }
        const pageSizes = [20, 50, 100]

        return l('div', async _ => {
            css`
                padding: 16px;
                width: 100%;
            `.apply(_)

            let zTable: Zone | null = null

            let year = years[0]

            let tasks: Array<ArchiveTask> = await fetchByYear(year)
            let pageSize = 20
            let pageNumber = 1
            const pageCount = () => Math.ceil(tasks.length / pageSize)

            lz(_, 'div', (_, z) => {
                css`
                    font-size: 16px;
                    display: flex;
                    align-items: center;
                    gap: 8px;
                    margin-bottom: 16px;
                    &>select {
                        font-size: 16px;
                        height: 30px;
                        border-radius: 4px;
                        background-color: #ededed;
                        border: none;
                        outline: none;
                    }                    
                    &>button {
                        cursor: pointer;
                        width: 30px;
                        height: 30px;
                        background-color: #ededed;
                        border: none;
                        border-radius: 4px;
                    }
                    &>select:hover, &>button:hover {
                        background-color: #F872441A;
                    }    
                `.apply(_)

                l(_, 'span', _ => { _.innerText = 'Год' })
                l(_, 'select', _ => {
                    css`width: 64px;`.apply(_)
                    for (let y of years)
                        l(_, 'option', _ => {
                            _.value = y.toString()
                            _.text = y.toString()
                            _.selected = y.toString() == year.toString()
                        })
                    _.onchange = async ev =>
                        sync([zTable, z], [pageNumber = 1, year =
                            Number((ev.target as HTMLSelectElement).value), tasks = await fetchByYear(year)])
                })

                l(_, 'span', _ => { _.innerText = 'Показывать по' })
                l(_, 'select', _ => {
                    for (let size of pageSizes)
                        l(_, 'option', _ => {
                            _.value = size + ''
                            _.text = size + ''
                            _.selected = size == pageSize
                        })

                    _.onchange = ev =>
                        sync([zTable, z], [pageNumber = 1, pageSize =
                            parseInt((ev.target as HTMLSelectElement).value)])
                })

                l(_, 'span', _ => { _.innerText = 'Страница ' })
                l(_, 'button', _ => {
                    css`
                        background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-chevron-left"><polyline points="15 18 9 12 15 6"/></svg>') no-repeat 6px 7px;
                    `.apply(_)
                    _.onclick = () => sync([zTable, z],
                        [pageNumber = Math.max(pageNumber - 1, 1)])
                })

                l(_, 'span', _ => { _.innerText = pageNumber + '' })

                l(_, 'button', _ => {
                    css`
                        background: url('data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-chevron-right"><polyline points="9 18 15 12 9 6"/></svg>') no-repeat 7px 7px;
                    `.apply(_)
                    _.onclick = () => sync([zTable, z],
                        [pageNumber = Math.min(pageNumber + 1, pageCount())])
                })
                l(_, 'span', _ => { _.innerText = 'из ' + pageCount() })
            })

            zTable = lz(_, 'table', _ => {
                stTable.apply(_)

                l(_, 'thead', _ => {
                    l(_, 'th', _ => { _.innerText = 'ID' })
                    l(_, 'th', _ => { _.innerText = 'Название' })
                    l(_, 'th', _ => { _.innerText = 'Дата архивации' })
                    l(_, 'th', _ => { _.innerText = 'Статус' })
                    l(_, 'th', _ => { _.innerText = 'Волонтеров' })
                    l(_, 'th', _ => { _.innerText = 'Ведущий' })
                })
                l(_, 'tbody', _ => {
                    for (let i = (pageNumber - 1) * pageSize; i < pageNumber * pageSize && i < tasks.length; i++) {
                        const task = tasks[i]
                        l(_, 'tr', _ => {
                            l(_, 'td', _ => { _.innerText = '#' + task.id })
                            l(_, 'td', _ => {
                                l(_, 'a', _ => {
                                    _.innerText = task.name
                                    _.href = this.taskEditLink.href(task.id)
                                })
                            })
                            l(_, 'td', _ => { _.innerText = task.archiveDate })
                            l(_, 'td', _ => { 
                                l(_, new TaskStatusView(task.status))
                            })
                            l(_, 'td', _ => { _.innerText = task.volonteerCount + '' })
                            l(_, 'td', _ => { _.innerText = task.leaderTgName })
                        })
                    }
                })
            })
        })
    }
}