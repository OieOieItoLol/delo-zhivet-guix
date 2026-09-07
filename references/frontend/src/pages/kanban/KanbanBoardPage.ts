import { apiCall, l, type AsyncClassComponent, css, lz, sync, Zone } from '../../lib';
import type { Link } from "../../lib/router";
import { Stage } from "../../stage";
import { stCheckbox } from '../../uikit/Checkbox';
import './KanbanBoardPage.css'

type TaskType = 'Ecological' | 'Social'

export type Tag = {
    id: Number,
    name: string,
}

export type TaskShort = {
    id: number,
    name: string,
    status: string,
    tgName: string,
    createDate: string,
    statusDaysCount: number,
    volunteerCount: number,
    managerDistance: number,
    photoCount: number,
    hasChatLink: boolean,
    tags: Array<Tag>,
    systemTags: Array<string>,
}

class LSValue<T> {
    private value: T
    LSKey: string

    constructor(
        LSKey: string,
        dflt: T
    ) { 
        let value = localStorage.getItem(LSKey)
        this.value = (value == null ? dflt : JSON.parse(value))
        this.LSKey = LSKey
    }

    set(value: T) {
        localStorage.setItem(this.LSKey, JSON.stringify(value))
        this.value = value
    }

    get() {
        return this.value
    }
}

export class KanbanBoardPage implements AsyncClassComponent<HTMLDivElement> {

    constructor(
        readonly taskEditLink: Link<number>,
        readonly taskType: TaskType
    ) { }

    mount(): Promise<HTMLDivElement> {
        let isSortOpen = false
        const dateSortEnabled = new LSValue('dateSortEnabled', false)
        const distanceSortEnabled = new LSValue('distanceSortEnabled', false)
        const volunteerCountSortEnabled = new LSValue('volunteerCountSortEnabled', false)

        let isFilterOpen = false
        const withChatFilter = new LSValue('withChatFilter', false)
        const withoutChatFilter = new LSValue('withoutChatFilter', false)
        const expiredFilter = new LSValue('expiredFilter', false)
        const withTagsFilter = new LSValue('withTagsFilter', false)

        let boardZone: Zone | null

        return l('div', async _ => {
            _.className = 'pages-kanban-board'

            let tasks = await apiCall<Array<TaskShort>>('/manager/task/tasksShort', this.taskType)
            l(_, 'div', _ => {
                css`
                    display: flex;
                    align-self: flex-end;
                    `.apply(_)

                lz(_, 'div', (_, z) => {
                    css`
                        align-self: flex-end;
                        `.apply(_)
                    _.onmouseenter = () => {if (!isSortOpen) sync([z], [isSortOpen = !isSortOpen])}
                    _.onmouseleave = () => {if (isSortOpen) sync([z], [isSortOpen = !isSortOpen])}
    
                    l(_, 'div', _ => {
                        _.className = 'option-button'
    
                        l(_, 'div', _ => { 
                            css`
                                width: 14px;
                                background: url("/public/icons/sort-descending.svg") no-repeat 0px 0px;
                                `.apply(_)
                        })
                        l(_, 'div', _ => {
                            css`
                                font-size: 14px;
                                margin-right: auto;
                                margin-left: 8px;
                                `.apply(_)
                            _.innerText = 'Сортировка' 
                        })
                    })
    
                    if(!isSortOpen) return
                    
                    l(_, 'div', _ => {
                        _.className = 'sort-items'
                        
                        l(_, 'div', _ => {
                            _.onclick = __ => sync([boardZone, z], [dateSortEnabled.set(!dateSortEnabled.get()), distanceSortEnabled.set(false), volunteerCountSortEnabled.set(false)])
                            dateSortEnabled.get() ? _.classList.add('active') : _.classList.remove('active')
                            
                            l(_, 'span', _ => {
                                _.innerText = 'По дате'
                            })
                        })
                        l(_, 'div', _ => {
                            _.onclick = __ => sync([boardZone, z], [distanceSortEnabled.set(!distanceSortEnabled.get()), dateSortEnabled.set(false), volunteerCountSortEnabled.set(false)])
                            distanceSortEnabled.get() ? _.classList.add('active') : _.classList.remove('active')

                            l(_, 'span', _ => {
                                _.innerText = 'По удаленности'
                            })
                        })
                        l(_, 'div', _ => {
                            _.onclick = __ => sync([boardZone, z], [volunteerCountSortEnabled.set(!volunteerCountSortEnabled.get()), dateSortEnabled.set(false), distanceSortEnabled.set(false)])
                            volunteerCountSortEnabled.get() ? _.classList.add('active') : _.classList.remove('active')

                            l(_, 'span', _ => {
                                _.innerText = 'По количеству волонтеров'
                            })
                        })
                    })
                })
    
                lz(_, 'div', (_, z) => {
                    css`
                        margin-left: 14px;
                        align-self: flex-end;
                    `.apply(_)
                    _.onmouseenter = () => {if (!isFilterOpen) sync([z], [isFilterOpen = !isFilterOpen])}
                    _.onmouseleave = () => {if (isFilterOpen) sync([z], [isFilterOpen = !isFilterOpen])}
                    
                    l(_, 'div', _ => {
                        _.className = 'option-button'
    
                        l(_, 'div', _ => { 
                            css`
                                width: 14px;
                                background: url("/public/icons/filter.svg") no-repeat 0px 0px;
                                `.apply(_)
                        })
                        l(_, 'div', _ => {
                            css`
                                font-size: 14px;
                                margin-right: auto;
                                margin-left: 8px;
                                `.apply(_)
                            _.innerText = 'Фильтр' 
                        })
                    })
    
                    if(!isFilterOpen) return
                    
                    l(_, 'div', _ => {
                        _.className = 'filter-items'
                        
                        l(_, 'div', _ => {
                            l(_, 'input', _ => {
                                _.type = 'checkbox'
                                _.checked = withChatFilter.get()
                                stCheckbox.apply(_)
                                _.onchange = __ => sync([boardZone, z], [withChatFilter.set(!withChatFilter.get())])
                            })
                            l(_, 'span', _ => {
                                _.innerText = 'С чатом'
                            })
                        })
                        l(_, 'div', _ => {
                            l(_, 'input', _ => {
                                _.type = 'checkbox'
                                _.checked = withoutChatFilter.get()
                                stCheckbox.apply(_)
                                _.onchange = __ => sync([boardZone, z], [withoutChatFilter.set(!withoutChatFilter.get())])
                            })
                            l(_, 'span', _ => {
                                _.innerText = 'Без чата'
                            })
                        })
                        l(_, 'div', _ => {
                            l(_, 'input', _ => {
                                _.type = 'checkbox'
                                _.checked = expiredFilter.get()
                                stCheckbox.apply(_)
                                _.onchange = __ => sync([boardZone, z], [expiredFilter.set(!expiredFilter.get())])
                            })
                            l(_, 'span', _ => {
                                _.innerText = 'Просроченные'
                            })
                        })
                        l(_, 'div', _ => {
                            l(_, 'input', _ => {
                                _.type = 'checkbox'
                                _.checked = withTagsFilter.get()
                                stCheckbox.apply(_)
                                _.onchange = __ => sync([boardZone, z], [withTagsFilter.set(!withTagsFilter.get())])
                            })
                            l(_, 'span', _ => {
                                _.innerText = 'С тэгами'
                            })
                        })
                    })
                })
            })
            
            
            boardZone = lz(_, 'div', _ => {
                _.className = 'card-board'
                l(_, 'div', _ => {
                    _.className = 'card-board-inner'

                    let tasksFiltered = tasks.filter(
                        t => !((withChatFilter.get() && !t.hasChatLink) ||
                               (withoutChatFilter.get() && t.hasChatLink) || 
                               (expiredFilter.get() && !t.systemTags.includes('Просрочена')) || 
                               (withTagsFilter.get() && t.tags.length == 0)
                               ))

                    let taskSorted = tasksFiltered.sort(
                        (t1, t2) => {
                            if (dateSortEnabled.get()) {
                                if (t1.createDate > t2.createDate) return 1
                                if (t2.createDate > t1.createDate) return -1
                            }
                            if (distanceSortEnabled.get()) {
                                if (t1.managerDistance < t2.managerDistance) return 1
                                if (t2.managerDistance < t1.managerDistance) return -1
                            }
                            if (volunteerCountSortEnabled.get()) {
                                if (t1.volunteerCount > t2.volunteerCount) return 1
                                if (t2.volunteerCount > t1.volunteerCount) return -1
                            }

                            return 0
                        }
                    )

                    const grouped = Map.groupBy(taskSorted, t => t.status)
                    const ofStatus = (st: string) => grouped.get(st) || []

                    l(_, new Stage(this.taskEditLink, 'Новые', ofStatus('New')))
                    l(_, new Stage(this.taskEditLink, 'В обработке', ofStatus('InProcessing')))
                    l(_, new Stage(this.taskEditLink, 'В планах', ofStatus('InPlan')))
                    l(_, new Stage(this.taskEditLink, 'Выполненые', ofStatus('Done')))
                    l(_, new Stage(this.taskEditLink, 'Неактивные', ofStatus('Inactive')))
                })
            })
        })
    }
}