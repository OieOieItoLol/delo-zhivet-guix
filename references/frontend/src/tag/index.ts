import { l, type ClassComponent } from '../lib.ts'
import './tag.css'


export function tagColor(name: string) {
    const hashCode = (s: string) => {
        for (var i = 0, h = 0; i < s.length; i++)
            h = Math.imul(31, h) + s.charCodeAt(i) | 0;
        return Math.abs(h);
    }

    switch (hashCode(name) % 5) {
        case 0: return '#30B0C7';
        case 1: return '#34C759';
        case 2: return '#FF3B30';
        case 3: return '#FF2D55';
        default: return '#007AFF';
    }
}

export class Tag implements ClassComponent<HTMLSpanElement> {
    constructor(
        readonly name: string
    ) { }

    mount(): HTMLSpanElement {


        return l('span', _ => {
            _.className = 'tag'
            _.style.backgroundColor = tagColor(this.name)
            _.innerText = this.name
        })
    }
}