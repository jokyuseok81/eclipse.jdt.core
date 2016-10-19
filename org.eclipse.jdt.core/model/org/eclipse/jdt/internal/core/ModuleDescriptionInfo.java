/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.ExportReference;
import org.eclipse.jdt.internal.compiler.ast.ModuleDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ModuleReference;
import org.eclipse.jdt.internal.compiler.env.IModule;

public class ModuleDescriptionInfo extends AnnotatableInfo implements IModule {

	protected static final char[][] NO_USES = new char[0][0];

	protected IJavaElement[] children = JavaElement.NO_ELEMENTS;

	ModuleReferenceInfo[] requires;
	PackageExportInfo[] exports;
	ServiceInfo[] services;
	char[][] usedServices;
	IModuleDescription handle;
	char[] name;

	static class ModuleReferenceInfo extends MemberElementInfo implements IModule.IModuleReference {
		char[] name;
		boolean isPublic;
		public char[] name() {
			return this.name;
		}
		public boolean isPublic() {
			return this.isPublic;
		}
	}
	static class PackageExportInfo extends MemberElementInfo implements IModule.IPackageExport {
		char[] pack;
		char[][] target;
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			
			buffer.append(this.pack);
			if (this.target != null) {
				buffer.append(this.target);
			}
			buffer.append(';');
			return buffer.toString();
		}

		@Override
		public char[] name() {
			return this.pack;
		}

		@Override
		public char[][] exportedTo() {
			return this.target;
		}
	}
	static class ServiceInfo extends MemberElementInfo implements IModule.IService {
		char[] serviceName;
		char[] implName;
		@Override
		public char[] name() {
			return this.serviceName;
		}
		@Override
		public char[] with() {
			return this.implName;
		}
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("provides "); //$NON-NLS-1$
			buffer.append(this.serviceName);
			buffer.append(" with "); //$NON-NLS-1$
			buffer.append(this.implName);
			buffer.append(';');
			return buffer.toString();
		}
	}

	public static ModuleDescriptionInfo createModule(ModuleDeclaration module) {
		ModuleDescriptionInfo mod = new ModuleDescriptionInfo();
		mod.name = module.moduleName;
		if (module.requiresCount > 0) {
			ModuleReference[] refs = module.requires;
			mod.requires = new ModuleReferenceInfo[refs.length];
			for (int i = 0; i < refs.length; i++) {
				mod.requires[i] = new ModuleReferenceInfo();
				mod.requires[i].name = CharOperation.concatWith(refs[i].tokens, '.'); // Check why ModuleReference#tokens must be a char[][] and not a char[] or String;
				mod.requires[i].isPublic = refs[i].isPublic();
			}
		} else {
			mod.requires = new ModuleReferenceInfo[0];
		}
		if (module.exportsCount > 0) {
			ExportReference[] refs = module.exports;
			mod.exports = new PackageExportInfo[refs.length];
			for (int i = 0; i < refs.length; i++) {
				PackageExportInfo exp = createPackageExport(refs, i);
				mod.exports[i] = exp;
			}
		} else {
			mod.exports = new PackageExportInfo[0];
		}
		return mod;
	}

	private static PackageExportInfo createPackageExport(ExportReference[] refs, int i) {
		ExportReference ref = refs[i];
		PackageExportInfo exp = new PackageExportInfo();
		exp.pack = CharOperation.concatWith(ref.tokens, '.');
		ModuleReference[] imp = ref.targets;
		if (imp != null) {
			exp.target = new char[imp.length][];
			for(int j = 0; j < imp.length; j++) {
				exp.target[j] = imp[j].moduleName;
			}
		}
		return exp;
	}

	protected void setHandle(IModuleDescription handle) {
		this.handle = handle;
	}

	public IJavaElement[] getChildren() {
		return this.children;
	}

	@Override
	public char[] name() {
		return this.name;
	}

	@Override
	public IModule.IModuleReference[] requires() {
		return this.requires;
	}
	@Override
	public IPackageExport[] exports() {
		return this.exports;
	}

	@Override
	public char[][] uses() {
		return this.usedServices;
	}

	@Override
	public IService[] provides() {
		return this.services;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer(getClass().getName());
		toStringContent(buffer);
		return buffer.toString();
	}
	protected void toStringContent(StringBuffer buffer) {
		buffer.append("\nmodule "); //$NON-NLS-1$
		buffer.append(this.name).append(' ');
		buffer.append('{').append('\n');
		if (this.requires != null && this.requires.length > 0) {
			buffer.append('\n');
			for(int i = 0; i < this.requires.length; i++) {
				buffer.append("\trequires "); //$NON-NLS-1$
				if (this.requires[i].isPublic) {
					buffer.append("public "); //$NON-NLS-1$
				}
				buffer.append(this.requires[i].name);
				buffer.append(';').append('\n');
			}
		}
		if (this.exports != null && this.exports.length > 0) {
			buffer.append('\n');
			for(int i = 0; i < this.exports.length; i++) {
				buffer.append("\texports "); //$NON-NLS-1$
				buffer.append(this.exports[i].toString()).append('\n');
			}
		}
		//TODO add the rest of the stuff
		buffer.append('\n').append('}').toString();
	}
}
