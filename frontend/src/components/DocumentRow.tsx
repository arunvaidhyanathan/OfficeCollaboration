import { formatDistanceToNow } from 'date-fns';
import { fileTypeOf } from '../lib/fileType';
import InlineRenameLabel from './InlineRenameLabel';
import type { DocumentSummary } from '../lib/api';

interface DocumentRowProps {
  doc: DocumentSummary;
  onOpen: () => void;
  onRename: (newName: string) => void;
}

export default function DocumentRow({ doc, onOpen, onRename }: DocumentRowProps) {
  const { icon: Icon, colorClass, bgClass } = fileTypeOf(doc.name);

  return (
    <div
      onClick={onOpen}
      className="group flex cursor-pointer items-center gap-3 border-b border-gray-100 px-3 py-2.5 hover:bg-gray-50"
    >
      <div className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-md ${bgClass}`}>
        <Icon size={16} className={colorClass} />
      </div>
      <InlineRenameLabel name={doc.name} onRename={onRename} className="flex-1 truncate text-sm text-gray-800" />
      <span className="w-32 shrink-0 text-right text-xs text-gray-400">
        {formatDistanceToNow(new Date(doc.updatedAt), { addSuffix: true })}
      </span>
      <span className="w-16 shrink-0 text-right">
        <span className="rounded bg-gray-100 px-1.5 py-0.5 text-[11px] text-gray-600">{doc.permission}</span>
      </span>
    </div>
  );
}
