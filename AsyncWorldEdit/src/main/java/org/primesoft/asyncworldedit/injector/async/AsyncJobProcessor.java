/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution in source, use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2.  Redistributions of source code, with or without modification, in any form
 *     other then free of charge is not allowed,
 * 3.  Redistributions of source code, with tools and/or scripts used to build the 
 *     software is not allowed,
 * 4.  Redistributions of source code, with information on how to compile the software
 *     is not allowed,
 * 5.  Providing information of any sort (excluding information from the software page)
 *     on how to compile the software is not allowed,
 * 6.  You are allowed to build the software for your personal use,
 * 7.  You are allowed to build the software using a non public build server,
 * 8.  Redistributions in binary form in not allowed.
 * 9.  The original author is allowed to redistrubute the software in bnary form.
 * 10. Any derived work based on or containing parts of this software must reproduce
 *     the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the
 *     derived work.
 * 11. The original author of the software is allowed to change the license
 *     terms or the entire license of the software as he sees fit.
 * 12. The original author of the software is allowed to sublicense the software
 *     or its parts using any license terms he sees fit.
 * 13. By contributing to this project you agree that your contribution falls under this
 *     license.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.primesoft.asyncworldedit.injector.async;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.entity.Player;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerManager;
import org.primesoft.asyncworldedit.api.utils.IFuncParamEx;
import org.primesoft.asyncworldedit.api.worldedit.ICancelabeEditSession;
import org.primesoft.asyncworldedit.api.worldedit.IThreadSafeEditSession;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.worldedit.BaseTask;
import org.primesoft.asyncworldedit.configuration.WorldeditOperations;
import org.primesoft.asyncworldedit.injector.classfactory.IEditSessionJob;
import org.primesoft.asyncworldedit.injector.classfactory.IJob;
import org.primesoft.asyncworldedit.injector.classfactory.IJobProcessor;
import org.primesoft.asyncworldedit.platform.api.IScheduler;
import org.primesoft.asyncworldedit.utils.SchedulerUtils;

/**
 *
 * @author SBPrime
 */
class AsyncJobProcessor implements IJobProcessor {

    /**
     * The AWE core
     */
    private final IAsyncWorldEditCore m_aweCore;

    /**
     * Bukkit schedule
     */
    private final IScheduler m_schedule;

    /**
     * Async block placer
     */
    protected final IBlockPlacer m_blockPlacer;

    /**
     * The layer manager
     */
    private final IPlayerManager m_playerManager;

    public AsyncJobProcessor(IAsyncWorldEditCore aweCore) {
        m_aweCore = aweCore;
        m_schedule = m_aweCore.getPlatform().getScheduler();
        m_blockPlacer = m_aweCore.getBlockPlacer();
        m_playerManager = m_aweCore.getPlayerManager();
    }

    /**
     * This function checks if async mode is enabled for specific command
     *
     * @param operation
     * @return
     */
    public boolean checkAsync(IPlayerEntry player, WorldeditOperations operation) {
        return ConfigProvider.isAsyncAllowed(operation) && player.getAweMode();
    }

    /**
     * This function checks if async mode is enabled for specific command
     *
     * @param operationName
     * @return
     */
    public boolean checkAsync(IPlayerEntry player, String operationName) {
        try {
            return checkAsync(player, WorldeditOperations.valueOf(operationName));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void executeJob(Player player, final IJob job) {
        if (job == null) {
            return;
        }

        final IPlayerEntry playerEntry = m_playerManager.getPlayer(player.getUniqueId());
        final String name = job.getName();
        boolean async = checkAsync(playerEntry, name);

        if (!async) {
            try {
                job.execute();
            } catch (Exception ex) {
                ErrorHandler.handleError(playerEntry, name, null, ex);
            }
            return;
        }

        final int jobId = m_blockPlacer.getJobId(playerEntry);
        final JobEntry jobEntry = new JobEntry(playerEntry, jobId, name);
        m_blockPlacer.addJob(playerEntry, jobEntry);
        SchedulerUtils.runTaskAsynchronously(m_schedule, new BaseTask(null, playerEntry,
                name, m_blockPlacer, jobEntry) {
                    @Override
                    protected Object doRun() throws MaxChangedBlocksException {
                        try {
                            job.execute();

                            return 0;
                        } catch (Exception ex) {
                            ErrorHandler.handleError(playerEntry, name, m_cancelableEditSession, ex);
                            return 0;
                        }
                    }

                    @Override
                    protected void doPostRun(Object result) {
                    }
                });
    }

    @Override
    public void executeJob(Player player, EditSession es, final IEditSessionJob job) {
        if (job == null) {
            return;
        }

        final IPlayerEntry playerEntry = m_playerManager.getPlayer(player.getUniqueId());
        final String name = job.getName();
        boolean async = checkAsync(playerEntry, name);

        if (!async) {
            try {
                job.execute(es);
            } catch (Exception ex) {
                ErrorHandler.handleError(playerEntry, name, null, ex);
            }
            return;
        }

        IThreadSafeEditSession itses = es instanceof IThreadSafeEditSession ? (IThreadSafeEditSession) es : null;
        if (itses == null) {
            ExceptionHelper.printException(new Exception("Expected " + IThreadSafeEditSession.class.getName()), "Unable to process async job");
            return;
        }

        //m_blockPlacer.performAsAsyncJob(null, playerEntry, name, null);
        m_blockPlacer.performAsAsyncJob(itses, playerEntry, name, new IFuncParamEx<Integer, ICancelabeEditSession, MaxChangedBlocksException>() {
            @Override
            public Integer execute(ICancelabeEditSession param) throws MaxChangedBlocksException {
                if (!(param instanceof EditSession)) {
                    ExceptionHelper.printException(new Exception("Expected " + EditSession.class.getName()), "Unable to process async job");
                    return 0;
                }

                try {
                    job.execute((EditSession) param);
                } catch (Exception ex) {
                    ErrorHandler.handleError(playerEntry, name, null, ex);
                }
                return 0;
            }

        });
    }
}
