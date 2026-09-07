import { useEffect, useRef, useState } from 'react';
import { Plus, ChevronDown, FileText, Table, Presentation, UploadCloud } from 'lucide-react';

interface NewDocumentMenuProps {
  onCreateBlank: (type: 'docx' | 'xlsx' | 'pptx') => void;
  onUploadClick: () => void;
  busy: boolean;
}

export default function NewDocumentMenu({ onCreateBlank, onUploadClick, busy }: NewDocumentMenuProps) {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function onClickOutside(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener('mousedown', onClickOutside);
    return () => document.removeEventListener('mousedown', onClickOutside);
  }, []);

  function pick(action: () => void) {
    setOpen(false);
    action();
  }

  return (
    <div ref={ref} className="relative">
      <div className="flex items-stretch rounded-lg bg-blue-600 text-white shadow-sm hover:bg-blue-700 transition-colors">
        <button
          type="button"
          disabled={busy}
          onClick={onUploadClick}
          className="flex items-center gap-2 px-3 py-2 text-sm font-medium disabled:opacity-60"
        >
          <Plus size={16} />
          New
        </button>
        <button
          type="button"
          disabled={busy}
          onClick={() => setOpen((v) => !v)}
          aria-label="More new-document options"
          className="flex items-center px-2 border-l border-white/20 disabled:opacity-60"
        >
          <ChevronDown size={14} />
        </button>
      </div>

      {open && (
        <div className="absolute left-0 mt-1 w-52 rounded-lg border border-gray-200 bg-white py-1 shadow-lg z-20">
          <button
            type="button"
            onClick={() => pick(onUploadClick)}
            className="flex w-full items-center gap-2 px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
          >
            <UploadCloud size={16} className="text-gray-500" />
            Upload file
          </button>
          <div className="my-1 border-t border-gray-100" />
          <button
            type="button"
            onClick={() => pick(() => onCreateBlank('docx'))}
            className="flex w-full items-center gap-2 px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
          >
            <FileText size={16} className="text-word" />
            Word Document
          </button>
          <button
            type="button"
            onClick={() => pick(() => onCreateBlank('xlsx'))}
            className="flex w-full items-center gap-2 px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
          >
            <Table size={16} className="text-sheet" />
            Spreadsheet
          </button>
          <button
            type="button"
            onClick={() => pick(() => onCreateBlank('pptx'))}
            className="flex w-full items-center gap-2 px-3 py-2 text-sm text-gray-700 hover:bg-gray-50"
          >
            <Presentation size={16} className="text-slide" />
            Presentation
          </button>
        </div>
      )}
    </div>
  );
}
