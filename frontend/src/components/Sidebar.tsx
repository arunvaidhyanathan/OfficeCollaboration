import { Files, FileText, Table, Presentation, Trash2 } from 'lucide-react';
import NewDocumentMenu from './NewDocumentMenu';
import type { FileKind } from '../lib/fileType';

export type FilterValue = FileKind | 'all';

interface SidebarProps {
  filter: FilterValue;
  onFilterChange: (filter: FilterValue) => void;
  onCreateBlank: (type: 'docx' | 'xlsx' | 'pptx') => void;
  onUploadClick: () => void;
  busy: boolean;
}

const NAV_ITEMS: { value: FilterValue; label: string; icon: typeof Files }[] = [
  { value: 'all', label: 'All Documents', icon: Files },
  { value: 'docx', label: 'Word (.docx)', icon: FileText },
  { value: 'xlsx', label: 'Sheet (.xlsx)', icon: Table },
  { value: 'pptx', label: 'Slides (.pptx)', icon: Presentation },
];

export default function Sidebar({ filter, onFilterChange, onCreateBlank, onUploadClick, busy }: SidebarProps) {
  return (
    <aside className="w-60 shrink-0 border-r border-gray-200 bg-gray-50 flex flex-col p-4 gap-4">
      <NewDocumentMenu onCreateBlank={onCreateBlank} onUploadClick={onUploadClick} busy={busy} />

      <nav className="flex flex-col gap-1">
        {NAV_ITEMS.map(({ value, label, icon: Icon }) => (
          <button
            key={value}
            type="button"
            onClick={() => onFilterChange(value)}
            className={`flex items-center gap-2.5 rounded-md px-2.5 py-2 text-sm text-left transition-colors ${
              filter === value ? 'bg-blue-100 text-blue-700 font-medium' : 'text-gray-600 hover:bg-gray-100'
            }`}
          >
            <Icon size={16} />
            {label}
          </button>
        ))}
      </nav>

      <div className="mt-auto">
        <button
          type="button"
          disabled
          title="Coming soon"
          className="flex items-center gap-2.5 rounded-md px-2.5 py-2 text-sm text-gray-400 cursor-not-allowed w-full text-left"
        >
          <Trash2 size={16} />
          Trash
        </button>
      </div>
    </aside>
  );
}
