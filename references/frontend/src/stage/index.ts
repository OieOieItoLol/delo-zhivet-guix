
import { l, type ClassComponent } from '../lib.ts'
import type { Link } from '../lib/router.ts'
import type { TaskShort } from '../pages/kanban/KanbanBoardPage.ts'
import { Card } from './card/index.ts'
import './stage.css'

export class Stage implements ClassComponent<HTMLDivElement> {

    constructor(
        readonly taskEditLink: Link<number>,
        readonly statusName: string,
        readonly tasks: Array<TaskShort>,
    ) { }

    mount(): HTMLDivElement {
        return l('div', _ => {
            _.className = 'stage'

            l(_, 'div', _ => {
                _.className = 'control'
                //l(_, 'div', _ => { _.innerText = '' + cardCount })
                l(_, 'div', _ => { _.innerText = '' + this.tasks.length })
                l(_, 'div', _ => { _.innerText = this.statusName })
                l(_, 'div', _ => { })
            })

            l(_, 'div', _ => {
                _.className = 'container'
                for (let task of this.tasks)
                    l(_, new Card(this.taskEditLink, task))
            })
        })
    }
}