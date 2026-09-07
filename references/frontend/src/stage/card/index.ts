// import type { TaskShort } from '../../app.ts'
import { css, l, type ClassComponent } from '../../lib.ts'
import type { Link } from '../../lib/router.ts'
import type { TaskShort } from '../../pages/kanban/KanbanBoardPage.ts'
import { Tag } from '../../tag/index.ts'
import './card.css'


export class Card implements ClassComponent<HTMLDivElement> {
    constructor(
        readonly taskEditLink: Link<number>,
        readonly task: TaskShort,
    ) { }

    mount(): HTMLDivElement {
        return l('div', _ => {
            _.className = 'card'
            l(_, 'div', _ => {
                _.className = 'tags'
                for (let tag of this.task.systemTags)
                    l(_, new Tag(tag))
            })
            l(_, 'span', _ => {
                css`cursor: pointer;`.apply(_)
                _.innerText = this.task.name
                _.onclick = __ => window.location.href = this.taskEditLink.href(this.task.id)
            })
            l(_, 'div', _ => {
                _.className = 'id-and-creation-date'
                l(_, 'div', _ => { })
                l(_, 'span', _ => { _.innerText = new Date(this.task.createDate).toLocaleDateString() })
                l(_, 'span', _ => { _.innerText = '#' + this.task.id })
            })
            l(_, 'div', _ => {
                _.className = 'assignee'
                l(_, 'img', _ => { })
                l(_, 'span', _ => { _.innerText = this.task.tgName }) // FIXME: author ???
            })
            l(_, 'div', _ => {
                _.className = 'tags'
                for (let tag of this.task.tags)
                    l(_, new Tag(tag.name))
            })
            l(_, 'div', _ => {
                _.className = 'status'
                l(_, 'span', _ => { _.innerText = `В статусе ${this.task.statusDaysCount} дней` })
                l(_, 'span', _ => { _.innerText = '' + this.task.volunteerCount })
                l(_, 'span', _ => { _.innerText = '' + this.task.photoCount })
            })
        })
    }
}