import { formatDistanceToNow } from 'date-fns';
import { fileTypeOf } from '../lib/fileType';
import InlineRenameLabel from './InlineRenameLabel';
import type { DocumentSummary } from '../lib/api';

interface DocumentCardProps {
  doc: DocumentSummary;
  onOpen: () => void;
  onRename: (newName: string) => void;
}

export default function DocumentCard({ doc, onOpen, onRename }: DocumentCardProps) {
  const { icon: Icon, colorClass, bgClass } = fileTypeOf(doc.name);

  return (
    <div
      onClick={onOpen}
      className="group cursor-pointer rounded-xl border border-gray-200 bg-white p-4 hover:border-blue-300 hover:shadow-md transition-all"
    >
      <div className={`flex h-16 w-16 items-center justify-center rounded-lg ${bgClass} mb-3`}>
        <Icon size={28} className={colorClass} />
      </div>
      <InlineRenameLabel
        name={doc.name}
        onRename={onRename}
        className="block truncate text-sm font-medium text-gray-800"
      />
      <p className="mt-1 text-xs text-gray-400">
        Edited {formatDistanceToNow(new Date(doc.updatedAt), { addSuffix: true })}
      </p>
    </div>
  );
}
