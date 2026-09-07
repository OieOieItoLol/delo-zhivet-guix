
export type TaskType = 'Ecological' | 'Social'

export type TagData = {
    id: number,
    name: string;
    taskType: TaskType,
    isHiddenInBot: boolean,
    isArchived: boolean,
}